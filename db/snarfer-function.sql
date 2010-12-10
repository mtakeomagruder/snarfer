--**********************************************************************************************************************
--* JOURNAL_INSERT Function
--* 
--* Add a new journal entry for an object.
--**********************************************************************************************************************
create or replace function journal_insert (iObjectID integer, strAction text, strDetails text) RETURNS void AS $$
begin
    delete from journal
    where object_id = iObjectID
    and action = strAction;

    insert into journal (id, date, object_id, action, action_detail) 
                 values (nextval('journal_id_seq'), now(), iObjectID, strAction, strDetails);  
    return;
end;
$$ language 'plpgsql';

--**********************************************************************************************************************
--* SOURCE_INSERT Function
--* 
--* Add a new source.
--**********************************************************************************************************************
create or replace function source_insert (strTextID text, strName text, strURL text) returns int as $$
declare
    iSourceID int;
    strSourceName text;
    iSourceURLID int;
begin
    select id, name 
      into iSourceID, strSourceName
    from source
    where text_id = strTextID;

    if iSourceID is null then
        select nextval('object_id_seq') into iSourceID;

        insert into source (id, text_id, name)
                  values (iSourceID, strTextID, strName);
    else
        if strName <> strSourceName then
            update source
            set name = strName
            where id = iSourceID;
        end if;
    end if;

    select id into iSourceURLID
    from source_url
    where source_id = iSourceID
      and url = strURL;

    if iSourceURLID is null then
        select nextval('object_id_seq') into iSourceURLID;

        insert into source_url (id, source_id, url)
                  values (iSourceURLID, iSourceID, strURL);
    end if;

    return(iSourceURLID);
end;
$$ language 'plpgsql';

--**********************************************************************************************************************
--* BATCH_INSERT Function
--* 
--* Add a new batch.
--**********************************************************************************************************************
create or replace function batch_insert(dtDay date) returns int as $$
declare
    iBatchID int;
begin
    delete from batch
    where day = dtDay;

    select nextval('object_id_seq') into iBatchID;

    insert into batch (id, day)
               values (iBatchID, dtDay);

    return(iBatchID);
end;
$$ language 'plpgsql';

--**********************************************************************************************************************
--* ARTICLE_INSERT Function
--* 
--* Add a new article.
--**********************************************************************************************************************
create or replace function article_insert(iBatchID int, iSourceURLID int, iTier int, strText text, strTextURL text, 
                                          tImage bytea, strImageURL text) returns int as $$
declare
    iArticleID int;
    iImageID int;
    strImageMD5 text;
    iImageSize int;
    iCount int;
    strRandomID text;
    iRandomIDCount int;
begin
    select max(id) into iArticleID
    from article
    where lower(url) = lower(strTextURL);

    if iArticleID is not null then
        select count(article_id)
          into iCount
          from batch_article
         where batch_id = iBatchID
           and article_id = iArticleID;

         if iCount <> 0 then
             return(iArticleID);
         end if;
    end if;

    loop 
        strRandomID = lower(text_id_generate(8));

        select count(id) 
          into iRandomIDCount
          from article
         where random_id = strRandomID;

        exit when iRandomIDCount = 0;
    end loop;

    if iArticleID is null then
        strImageMD5 = encode(digest(tImage, 'md5'), 'hex');
        iImageSize = length(tImage);

        select id into iImageID
        from image 
        where md5 = strImageMD5
          and size = iImageSize;

        if iImageID is null then
            select nextval('object_id_seq') into iImageID;
            
            insert into image (id, size, md5, url, data)
                       values (iImageID, iImageSize, strImageMD5, strImageURL, tImage);
        end if;
        
        select nextval('object_id_seq') into iArticleID;

        insert into article (id, random_id, source_url_id, image_id, tier, url, data)
                     values (iArticleID, strRandomID, iSourceURLID, iImageID, iTier, strTextURL, strText);

    end if;

    insert into batch_article (batch_id, article_id)
                       values (iBatchID, iArticleID);

    return(iArticleID);
end;
$$ language 'plpgsql';

--**********************************************************************************************************************
--* TEXT_ID_GENERATE Function
--* 
--* Create a unique text ID.
--**********************************************************************************************************************
CREATE or replace FUNCTION text_id_generate(iSize int) RETURNS text
    AS $$
    DECLARE
    strLookup text;
    strTextID text;
    BEGIN
    strLookup = '23456789ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz';

    strTextID = substring(strLookup from ceil(random() * 49)::integer for 1);

    FOR i IN 1..iSize LOOP
        strTextID = strTextID || substring(strLookup from ceil(random() * 57)::integer for 1);
    END LOOP;

    return(strTextID);
    END;
$$
    LANGUAGE plpgsql;

create type article_record as
    (
    id int,
    random_id text,
    source_url_id int,
    tier int,
    size int,
    hash text,
    url text,
    data text,
    image_id int,
    image_size int,
    image_hash text,
    image_url text,
    image_data bytea,
    batch_id int,
    batch_day date
    );

--**********************************************************************************************************************
--* ARTICLE_LIST_GET Function
--* 
--* Get a limited list of articles for the batch and source.
--**********************************************************************************************************************
create or replace function article_list_get(iBatchID int, iSourceID int, iLimit int) returns setof article_record as $$
declare
    xArticle article_record%rowtype;
    iCount int;
    dtDay date;
begin
    select day
      into dtDay
      from batch
     where id = iBatchID;

    iCount = 0;
    
    for xArticle in 
        select article.id,
               article.random_id,
               article.source_url_id,
               article.tier,
               length(article.data),
               encode(digest(article.data, 'md5'), 'hex') as hash,
               article.url,
               article.data,
               image.id as image_id,
               image.size as image_size,
               image.md5 as image_hash,
               image.url as image_url,
               image.data as image_data,
               batch.id as batch_id,
               batch.day as batch_day
        from article, batch_article, image, source, source_url, batch
        where source.id = iSourceID
          and source.id = source_url.source_id
          and source_url.id = article.source_url_id
          and batch.day >= dtDay - interval '30 days'
          and batch.id <= iBatchID
          and batch.id = batch_article.batch_id
          and batch_article.article_id = article.id
          and article.image_id = image.id
        order by batch.day desc, article.tier, article.random_id
        limit iLimit
    loop
        iCount = iCount + 1;
        return next xArticle;
    end loop;
end;
$$ language 'plpgsql';

--**********************************************************************************************************************
--* VW_ARTICLE_BATCH_MIN View
--* 
--* Get the first batch where an article appeared.
--**********************************************************************************************************************
create or replace view vw_article_batch_min as
select article_id, min(batch_id) as batch_id
from batch_article
group by article_id;