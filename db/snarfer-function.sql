--******************************************************************************
--* Journal functions 
--******************************************************************************
CREATE or REPLACE function journal_insert (iObjectID integer, strAction text, strDetails text) RETURNS void AS $$
    BEGIN
    delete from journal
    where object_id = iObjectID
    and action = strAction;

    insert into journal (id, date, object_id, action, action_detail) 
                 values (nextval('journal_id_seq'), now(), iObjectID, strAction, strDetails);  
    return;
    END;
$$ LANGUAGE 'plpgsql';

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

create or replace function article_insert(iBatchID int, iSourceURLID int, iTier int, strText text, strTextURL text, tImage bytea, strImageURL text) returns int as $$
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

create or replace function article_list_get(iBatchID int, iSourceID int, iLimit int) returns setof article_record as $$
declare
    xArticle article_record%rowtype;
    iCount int;
begin
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
          and batch.id = iBatchID
          and batch.id = batch_article.batch_id
          and batch_article.article_id = article.id
          and article.image_id = image.id
        order by article.tier, article.random_id
        limit iLimit
    loop
        iCount = iCount + 1;
        return next xArticle;
    end loop;

    if iCount < iLimit then
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
            from article, image, source, source_url, vw_article_batch_min, batch
            where source.id = iSourceID
              and source.id = source_url.source_id
              and source_url.id = article.source_url_id
              and article.image_id = image.id
              and article.id in 
                  (
                  select article.id
                  from batch, article, batch batch_other, batch_article
                  where batch.id = 1686
                  and article.id = batch_article.article_id
                  and article.tier <> 1
                  and batch_article.batch_id = batch_other.id
                  and batch_other.day < batch.day
                  order by article.random_id
                  limit iLimit - iCount + 1
                  )
              and article.id = vw_article_batch_min.article_id
              and vw_article_batch_min.batch_id = batch.id
            order by batch.day desc, article.random_id
        loop
            return next xArticle;
        end loop;
    end if;
end;
$$ language 'plpgsql';

--select random_id, tier, batch_id, url from article_list_get(1686, 1661, 5);

--select source_insert('bbc', 'BBC News', 'http://news.bbc.co.uk');
--select batch_insert('01-DEC-2005');
--select article_insert(1, 1, 1, 'sample text 2', 'http://sample', '000', 'http://sampleimage');

create or replace view vw_article_batch_min as
select article_id, min(batch_id) as batch_id
from batch_article
group by article_id;

--******************************************************************************
--* Genetic functions
--******************************************************************************
create or replace function genetic_job_begin(strProjectID text, strGeneticProfileID text) returns int as $$
declare
    iProjectID int;
    bProjectActive boolean;
    iGeneticProfileID int;
    bGeneticProfileActive boolean;
    iGeneticProjectProfileID int;
    iProjectImageID int;    
    iLockID int;
    cProjectProfiles record;
begin
    select id, active
      into iProjectID, bProjectActive
      from project
     where text_id = lower(strProjectID);

    if iProjectID is null then
	return(-101);
    end if;

    if not bProjectActive then
	return(-103);
    end if;
    
    if strGeneticProfileID is not null then
        select id, active
          into iGeneticProfileID, bGeneticProfileActive
          from genetic_profile
         where text_id = lower(strGeneticProfileID);

        if iGeneticProfileID is null then
	    return(-102);
        end if;

        if not bGeneticProfileActive then
    	    return(-103);
        end if;

        select id
          into iGeneticProjectProfileID
          from genetic_project_profile
         where project_id = iProjectID
           and genetic_profile_id = iGeneticProfileID;

        if iGeneticProjectProfileID is null then
    	    return(-4);
        end if;
    end if;

    lock genetic_project_profile_image_lock;

    for cProjectProfiles in
        select genetic_project_profile.id
          from genetic_project_profile, genetic_profile
         where genetic_project_profile.project_id = iProjectID
           and (strGeneticProfileID is null or genetic_profile_id = iGeneticProfileID)
           and genetic_project_profile.genetic_profile_id = genetic_profile.id
           and genetic_profile.active = true
    loop
        select max(id)
          into iProjectImageID
          from project_image
         where project_id = iProjectID
           and id not in
        (
            select project_image_id
              from genetic_project_profile_image
             where genetic_project_profile_image.genetic_project_profile_id = cProjectProfiles.id
                union
            select genetic_project_profile_image_lock.project_image_id
              from genetic_project_profile_image_lock, journal
             where genetic_project_profile_image_lock.genetic_project_profile_id = cProjectProfiles.id
               and genetic_project_profile_image_lock.id = journal.object_id
               and journal.action = 'insert'
               and journal.date >= localtimestamp - interval '72 hours'
        );

	if iProjectImageID is not null then
            delete from genetic_project_profile_image_lock
             where genetic_project_profile_id = cProjectProfiles.id
               and project_image_id = iProjectImageID;
	
            select nextval('object_id_seq') into iLockID;

            insert into genetic_project_profile_image_lock (id, genetic_project_profile_id, project_image_id)
                                                    values (iLockID, cProjectProfiles.id, iProjectImageID);

            return(iLockID);    
        end if;            
    end loop;

    return(-105);
end;
$$ language 'plpgsql';

create or replace function genetic_job_complete(iLockID int, fFitness float, strXML text) returns int as $$
declare
    iGeneticProjectProfileID int;
    iProjectImageID int;    
    iGeneticProjectProfileImageID int;
begin
    lock genetic_project_profile_image_lock;
    
    select genetic_project_profile_id, project_image_id
      into iGeneticProjectProfileID, iProjectImageID
      from genetic_project_profile_image_lock
     where id = iLockID;

    if iProjectImageID is null then
	return(-201);
    end if;

    select nextval('object_id_seq') into iGeneticProjectProfileImageID;

    insert into genetic_project_profile_image (id, genetic_project_profile_id, project_image_id, fitness, data)
                                       values (iGeneticProjectProfileImageID, iGeneticProjectProfileID, iProjectImageID, fFitness, strXML);

    delete from genetic_project_profile_image_lock
     where genetic_project_profile_id = iGeneticProjectProfileID
       and project_image_id = iProjectImageID;

    return(iGeneticProjectProfileImageID);
end;
$$ language 'plpgsql';

create or replace function genetic_job_complete(iLockID int, fFitness float, strXML text, strMachineID text, strAddressLocal text, iThreadTotal int,
                                                iEvolutionAge int, iEvolutionGeneration int, fEvolutionTime float, iGrowthAge int, 
                                                fGrowthTime float, iPolishAge int, fPolishTime float) returns int as $$
declare
    iGeneticProjectProfileImageID int;
begin
    iGeneticProjectProfileImageID = genetic_job_complete(iLockID, fFitness, strXML);

    if iGeneticProjectProfileImageID < 0 then
        return(iGeneticProjectProfileImageID);
    end if;

    insert into genetic_project_profile_image_job (id, 
                                                   genetic_project_profile_image_id, 
                                                   machine_id, 
                                                   address, 
                                                   address_local, 
                                                   thread_total, 
                                                   evolution_age,
                                                   evolution_generation, 
                                                   evolution_time, 
                                                   growth_age, 
                                                   growth_time, 
                                                   polish_age, 
                                                   polish_time)
                                           values (nextval('object_id_seq'),
                                                   iGeneticProjectProfileImageID,
                                                   strMachineID,
                                                   inet_client_addr(),
                                                   inet(strAddressLocal),
                                                   iThreadTotal,
                                                   iEvolutionAge,
                                                   iEvolutionGeneration,
                                                   fEvolutionTime,
                                                   iGrowthAge,
                                                   fGrowthTime,
                                                   iPolishAge,
                                                   fPolishTime);
    
    return(iGeneticProjectProfileImageID);
end;
$$ language 'plpgsql';

--drop type genetic_profile_record cascade
create type genetic_profile_record as
    (
    population int,
    evolution_active boolean,
    evolution_algorithm text,
    evolution_crossover_first int,
    evolution_crossover_inc int,
    evolution_generation_limit int,
    evolution_time_limit int,
    growth_active boolean,
    growth_age_limit int,
    growth_time_limit int,
    polish_active boolean,
    polish_limit int,
    polish_age_limit int,
    polish_time_limit int,
    polygon_min int,
    polygon_max int,
    polygon_point_min int,
    polygon_point_max int
    );

create or replace function genetic_profile_get(iLockID int) returns setof genetic_profile_record as $$
declare
    xGeneticProfile genetic_profile_record%rowtype;
begin
    for xGeneticProfile in 
        select genetic_profile.population,
               genetic_profile.evolution_active,
               genetic_profile.evolution_algorithm,
               genetic_profile.evolution_crossover_first,
               genetic_profile.evolution_crossover_inc,
               genetic_profile.evolution_generation_limit,
               genetic_profile.evolution_time_limit,
               genetic_profile.growth_active,
               genetic_profile.growth_age_limit,
               genetic_profile.growth_time_limit,
               genetic_profile.polish_active,
               genetic_profile.polish_limit,
               genetic_profile.polish_age_limit,
               genetic_profile.polish_time_limit,
               genetic_profile.polygon_min,
               genetic_profile.polygon_max,
               genetic_profile.polygon_point_min,
               genetic_profile.polygon_point_max
        from genetic_project_profile_image_lock, genetic_project_profile, genetic_profile
        where genetic_project_profile_image_lock.id = iLockID
          and genetic_project_profile_image_lock.genetic_project_profile_id = genetic_project_profile.id
          and genetic_project_profile.genetic_profile_id = genetic_profile.id
    loop
        return next xGeneticProfile;
    end loop;
end;
$$ language 'plpgsql';

create or replace function genetic_image_get(iLockID int) returns bytea as $$
declare
    byImage bytea;
begin
    select image.data
      into byImage
      from genetic_project_profile_image_lock, project_image, image
     where genetic_project_profile_image_lock.project_image_id = project_image.id
       and project_image.image_id = image.id;

    return(byImage);
end;
$$ language 'plpgsql';
