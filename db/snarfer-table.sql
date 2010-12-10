/*
--drop table batch_article;
--drop table batch;
--drop table image;
--drop table article;
--drop table source_url;
--drop table source;
--drop table journal;
--drop table object;
*/

/***********************************************************************************************************************
* Use one sequence for all objects so there is no overlap.  This helps prevent join errors.
***********************************f************************************************************************************/
create sequence object_id_seq;

/***********************************************************************************************************************
* Sequence for jounal entries
***********************************f************************************************************************************/
create sequence journal_id_seq;

/***********************************************************************************************************************
* OBJECT Table
*
* Each object gets an entry in this table to enforce unique IDs and provide integrity for the journal.
***********************************f************************************************************************************/
create table object
    (
    id int
        constraint obj_id_nn not null
        constraint obj_pk primary key,
    type text
        constraint obj_type_nn not null
        constraint obj_type_ck check (type in ('source', 'source_url', 'article', 'image', 'batch', 'project'))
    );

create index obj_type_idx on object(type);

/***********************************************************************************************************************
* JOURNAL Table
*
* Each action on the database gets an entry in this table for auditing purposes.
***********************************f************************************************************************************/
create table journal
    (
    id integer
        constraint jrn_id_nn not null
        constraint jrn_pk primary key,
    date timestamp
        constraint jrn_date_nn not null,
    object_id integer
        constraint jrn_objectid_nn not null 
        constraint jrn_objectid_obj_id references object(id) on delete cascade on update cascade,
    action text
        constraint jrn_action_nn not null
        constraint jrn_action_ck check (action in ('insert', 'update', 'start', 'stop')),
    action_detail text
    );

create index jrn_date_idx on journal (date);
create index jrn_objectid_idx on journal (object_id);
create index jrn_action_idx on journal (action);

/***********************************************************************************************************************
* SOURCE Table
*
* Stores the news sources.
***********************************f************************************************************************************/
create table source
    (
    id int
        constraint src_id_nn not null
        constraint src_id_obj_id references object(id) on update cascade
        constraint src_pk primary key,
    text_id text
        constraint src_textid_nn not null,
    name text
        constraint src_name_nn not null
    );

create unique index src_textid_unq on source(text_id);
create unique index src_name_unq on source(name);

/***********************************************************************************************************************
* SOURCE_URL Table
*
* Stores the various URLs for a source.
***********************************f************************************************************************************/
create table source_url
    (
    id int
        constraint srcurl_id_nn not null
        constraint srcurl_id_obj_id references object(id) on update cascade
        constraint srcurl_pk primary key,
    source_id int
        constraint srcurl_id_nn not null
        constraint srcurl_id_src_id references source(id) on update cascade,
    url text
        constraint srcurl_id_nn not null
    );

/***********************************************************************************************************************
* IMAGE Table
*
* Stores article images.
***********************************f************************************************************************************/
create table image
    (
    id int
        constraint img_id_nn not null
        constraint img_id_obj_id references object(id) on update cascade
        constraint img_pk primary key,
    size int
        constraint img_size_nn not null
        constraint img_size_ck check (size > 0),
    md5 text
        constraint img_md5_nn not null
        constraint img_md5_ck check (length(md5) = 32),
    url text
        constraint img_md5_nn not null,
    data bytea
        constraint img_md5_nn not null
    );

create unique index img_sizemd5_unq on image(size, md5);

/***********************************************************************************************************************
* ARTICLE Table
*
* Stores articles.
***********************************f************************************************************************************/
create table article
    (
    id int
        constraint art_id_nn not null
        constraint art_id_obj_id references object(id) on update cascade
        constraint art_pk primary key,
    random_id text
        constraint art_randomid_nn not null,
    source_url_id int
        constraint art_srcurlid_nn not null
        constraint art_id_srcurl_id references source_url(id) on update cascade,
    image_id int
        constraint art_imgid_nn not null
        constraint art_id_img_id references image(id) on update cascade,
    tier int
        constraint art_tier_nn not null
        constraint art_tier_ck check (tier > 0),
    url text
        constraint art_url_nn not null,
    data text
        constraint art_data_nn not null
    );

create index art_imgid_idx on article(image_id);
create index art_sourceurlid_idx on article(source_url_id);
create unique index art_randomid_unq on article(random_id);

/***********************************************************************************************************************
* BATCH Table
*
* Stores a batch of articles for a given day.
***********************************f************************************************************************************/
create table batch
    (
    id int
        constraint btc_id_nn not null
        constraint btc_id_obj_id references object(id) on update cascade
        constraint btc_pk primary key,
    day date
        constraint btc_day_nn not null
    );

create unique index btc_day_unq on batch(day);

/***********************************************************************************************************************
* BATCH_ARTICLE Table
*
* Stores the articles related to a batch.  This is more efficient since duplicate articles can be stored once and then
* referenced from this table as often as they appear.
***********************************f************************************************************************************/
create table batch_article
    (
    batch_id int
        constraint btcart_btcid_nn not null
        constraint btcart_btcid_btc_id references batch(id) on delete cascade on update cascade,
    article_id int
        constraint btcart_artid_nn not null
        constraint btcart_artid_art_id references article(id) on delete cascade on update cascade,
    constraint btcart_pk primary key (batch_id, article_id)
    );