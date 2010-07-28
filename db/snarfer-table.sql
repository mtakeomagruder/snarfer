/*
drop table genetic_project_profile_image_job;
drop table genetic_project_profile_image_lock;
drop table genetic_project_profile_image;
drop table genetic_project_profile;
drop table genetic_profile;
drop table project_image;
drop table project;
----------------------------------------------
--drop table batch_article;
--drop table batch;
--drop table image;
--drop table article;
--drop table source_url;
--drop table source;
--drop table journal;
--drop table object;
*/

create sequence object_id_seq;
create sequence journal_id_seq;

create table object
    (
    id int
        constraint obj_id_nn not null
        constraint obj_pk primary key,
    type text
        constraint obj_type_nn not null
        constraint obj_type_ck check (type in ('source', 'source_url', 'article', 'image', 'batch', 'project', 'project_filter_image', 'project_image', 
                                               'genetic_profile', 'genetic_project_profile', 'genetic_project_profile_image', 'genetic_project_profile_image_lock',
                                               'genetic_project_profile_image_job'))
    );

create index obj_type_idx on object(type);

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

create table project
    (
    id int
        constraint prj_id_nn not null
        constraint prj_id_obj_id references object(id) on update cascade,
    text_id text
        constraint prj_textid_nn not null,
        constraint prj_textid_ck check (text_id = lower(text_id)),
    active boolean default true
        constraint prj_active_nn not null,
    begin_date timestamp
        constraint prj_begindate_ck check (begin_date is null or end_date is null or begin_date <= end_date),
    end_date timestamp,
    description text
        constraint prj_description_nn not null,
    constraint prj_pk primary key (id)
    );

create unique index prj_txtid_unq on project (text_id);

create table project_image
    (
    id int
        constraint prjimg_id_nn not null
        constraint prjimg_id_obj_id references object(id) on update cascade,
    project_id int
        constraint prjimg_prjid_nn not null
        constraint prjimg_prjid_prj_id references project(id) on update cascade,
    image_id int
        constraint prjimg_imgid_nn not null
        constraint prjimg_imgid_img_id references image(id) on update cascade,
    constraint prjimg_pk primary key (id)
    );

create unique index prjimg_prjidimgid_unq on project_image (project_id, image_id);

create table genetic_profile
    (
    id int
        constraint genprf_id_nn not null
        constraint genprf_id_obj_id references object(id) on update cascade,
    text_id text
        constraint genprf_textid_nn not null
        constraint genprf_textid_ck check (text_id = lower(text_id)),
    active boolean default true
        constraint genprf_active_nn not null,
    population int 
        constraint genprf_population_nn not null
        constraint genprf_population_ck check (population >= 4 and population <= 128),
    evolution_active boolean 
        constraint genprf_evoactive_nn not null,
        constraint genprf_evoactive_ck check (evolution_active or growth_active),
    evolution_algorithm text 
        constraint genprf_evoalgorithm_ck check ((not evolution_active and evolution_algorithm is null) or (evolution_active and evolution_algorithm is not null and evolution_algorithm in ('top_half_random_keep_best'))),
    evolution_crossover_first int 
        constraint genprf_evocrossfirst_ck check ((not evolution_active and evolution_crossover_first is null) or (evolution_active and evolution_crossover_first is not null and evolution_crossover_first >= 100 and evolution_crossover_first <= 100000)),
    evolution_crossover_inc int 
        constraint genprf_evocrossinc_ck check ((not evolution_active and evolution_crossover_inc is null) or (evolution_active and evolution_crossover_inc is not null and evolution_crossover_inc >= 100 and evolution_crossover_inc <= 10000)),
    evolution_generation_limit int 
        constraint genprf_evogenerationlimit_ck check ((not evolution_active and evolution_generation_limit is null) or (evolution_active and (evolution_generation_limit is not null or evolution_time_limit is not null) and evolution_generation_limit >= 1 and evolution_generation_limit <= 1000)),
    evolution_time_limit int
        constraint genprf_evotimelimit_ck check ((not evolution_active and evolution_time_limit is null) or (evolution_active and (evolution_generation_limit is not null or evolution_time_limit is not null) and evolution_time_limit >= 1 and evolution_time_limit <= 10080)),
    growth_active boolean 
        constraint genprf_grwactive_nn not null,
    growth_age_limit int 
        constraint genprf_grwagelimit_ck check ((not growth_active and growth_age_limit is null) or (growth_active and (growth_age_limit is not null or growth_time_limit is not null) is not null and growth_age_limit >= 100 and growth_age_limit <= 1000000000)),
    growth_time_limit int
        constraint genprf_grwtimelimit_ck check ((not growth_active and growth_time_limit is null) or (growth_active and (growth_age_limit is not null or growth_time_limit is not null) and growth_time_limit >= 1 and growth_time_limit <= 10080)),
    polish_active boolean 
        constraint genprf_polactive_nn not null,
    polish_limit int 
        constraint genprf_pollimit_ck check ((not polish_active and polish_limit is null) or (polish_active and polish_limit is not null and polish_limit >= 1 and polish_limit <= population)),
    polish_age_limit int 
        constraint genprf_polagelimit_ck check ((not polish_active and polish_age_limit is null) or (polish_active and (polish_age_limit is not null or polish_time_limit is not null) and polish_age_limit >= 100 and polish_age_limit <= 1000000000)),
    polish_time_limit int
        constraint genprf_poltimelimit_ck check ((not polish_active and polish_time_limit is null) or (polish_active and (polish_age_limit is not null or polish_time_limit is not null) and polish_time_limit >= 1 and polish_time_limit <= 10080)),
    polygon_min int default 1
        constraint genprf_plymin_nn not null,
        constraint genprf_plymin_ck check (polygon_min >= 1 and polygon_min <= 1024 and polygon_min <= polygon_max),
    polygon_max int default 128
        constraint genprf_plymax_nn not null,
        constraint genprf_plymax_ck check (polygon_max >= 1 and polygon_max <= 1024),
    polygon_point_min int default 3
        constraint genprf_plypntmin_nn not null,
        constraint genprf_plypntmin_ck check (polygon_point_min >= 3 and polygon_point_min <= 64 and polygon_point_min <= polygon_point_max),
    polygon_point_max int default 16
        constraint genprf_plypntmax_nn not null,
        constraint genprf_plypntmax_ck check (polygon_point_max >= 3 and polygon_point_max <= 64),
    constraint genprf_timeorage_ck check (((not evolution_active or (evolution_active and evolution_generation_limit is not null)) and 
                                           (not growth_active or (growth_active and growth_age_limit is not null)) and 
                                           (not polish_active or (polish_active and polish_age_limit is not null))
                                          ) or
				          ((not evolution_active or (evolution_active and evolution_time_limit is not null)) and 
                                           (not growth_active or (growth_active and growth_time_limit is not null)) and 
                                           (not polish_active or (polish_active and polish_time_limit is not null))
                                          )                                          
                                         ),
    constraint genprf_pk primary key (id)
    );
/*    color_change_min int
        constraint genprf_clrchgmin_nn not null,
        constraint genprf_clrchgmin_ck check (color_change_min >= 1 and color_change_min <= 64 and color_change_min <= color_change_max),
    color_change_max int
        constraint genprf_clrchgmax_nn not null,
        constraint genprf_clrchgmax_ck check (color_change_max >= 1 and color_change_max <= 64),
    alpha_min int
        constraint genprf_aphmin_nn not null,
        constraint genprf_aphmin_ck check (alpha_min >= 0 and alpha_min <= 255 and alpha_min <= alpha_max),
    alpha_max int
        constraint genprf_aphmax_nn not null,
        constraint genprf_aphmax_ck check (alpha_max >= 0 and alpha_max <= 255),
    alpha_change_min int
        constraint genprf_aphrchgmin_nn not null,
        constraint genprf_aphchgmin_ck check (alpha_change_min >= 1 and alpha_change_min <= 64 and alpha_change_min <= alpha_change_max),
    alpha_change_max int
        constraint genprf_aphchgmax_nn not null,
        constraint genprf_aphchgmax_ck check (alpha_change_max >= 1 and alpha_change_max <= 64),*/


/*??? point_min, point_max
??? all rates
???? mutate rate
??? color mins and maxs
??? what about limiting polygons to certain colors?

select * from genetic_profile
*/

create unique index genprf_txtid_unq on genetic_profile (text_id);

create table genetic_project_profile
    (
    id int
        constraint genprjprf_id_nn not null
        constraint genprjprf_id_obj_id references object(id) on update cascade,
    project_id int
        constraint genprjprf_prjid_nn not null
        constraint genprjprf_prjid_prj_id references project(id) on update cascade,
    genetic_profile_id int
        constraint genprjprf_genprfid_nn not null
        constraint genprjprf_genprfid_genprf_id references genetic_profile(id) on update cascade,
    constraint genprjprf_pk primary key (id)
    );

--drop index genprjprf_prjidgenprfid_unq
create unique index genprjprf_prjidgenprfid_unq on genetic_project_profile (project_id, genetic_profile_id);

create table genetic_project_profile_image
    (
    id int
        constraint genprjprfimg_id_nn not null
        constraint genprjprfimg_id_obj_id references object(id) on update cascade,
    genetic_project_profile_id int
        constraint genprjprfimg_genprjprfid_nn not null
        constraint genprjprfimg_genprjprfid_genprjprf_id references genetic_project_profile (id) on update cascade,
    project_image_id int
        constraint genprjprfimg_prjimgid_nn not null
        constraint genprjprfimg_prjimgid_img_id references project_image(id) on update cascade,
    fitness float
        constraint genprjprfimg_fitness_nn not null,
    data text
        constraint genprjprfimg_data_nn not null,
    constraint genprjprfimg_pk primary key (id)
    );

create unique index genprjprfimg_genprjprfidprjimgid_unq on genetic_project_profile_image (genetic_project_profile_id, project_image_id);
create index genprjprfimg_genprjprfid_idx on genetic_project_profile_image (genetic_project_profile_id);
        
create table genetic_project_profile_image_lock
    (
    id int
        constraint genprjprfimglck_id_nn not null
        constraint genprjprfimglck_id_obj_id references object(id) on update cascade,
    genetic_project_profile_id int
        constraint genprjprfimglck_genprjprfid_nn not null
        constraint genprjprfimglck_genprjprfid_genprjprf_id references genetic_project_profile(id) on update cascade,
    project_image_id int
        constraint genprjprfimglck_prjimgid_nn not null
        constraint genprjprfimglck_prjimgid_img_id references project_image(id) on update cascade,
    constraint genprjprfimglck_pk primary key (id)
    );

create unique index genprjprfimglck_genprjprflckprjimgid_unq on genetic_project_profile_image_lock (genetic_project_profile_id, project_image_id);
create index genprjprfimglck_genprjprfid_idx on genetic_project_profile_image_lock (genetic_project_profile_id);

create table genetic_project_profile_image_job
    (
    id int
        constraint genprjprfimgjob_id_nn not null
        constraint genprjprfimgjob_id_obj_id references object(id) on update cascade,
    genetic_project_profile_image_id int
        constraint genprjprfimgjob_genprjprfimgid_nn not null
        constraint genprjprfimgjob_genprjprfimgid_genprjprfimg_id references genetic_project_profile_image (id) on update cascade on delete cascade,
    machine_id text
        constraint genprjprfimgjob_machine_nn not null,
    address inet
        constraint genprjprfimgjob_address_nn not null,
    address_local inet,
    thread_total int        
        constraint genprjprfimgjob_threadtotal_nn not null
        constraint genprjprfimgjob_threadtotal_ck check (thread_total >= 1),
    evolution_age int
        constraint genprjprfimgjob_evoage_ck check (evolution_age >= 1),
    evolution_generation int
        constraint genprjprfimgjob_evogeneration_ck check (evolution_generation >= 1),
    evolution_time numeric(10, 3)
        constraint genprjprfimgjob_evotime_ck check (evolution_time > 0),
    growth_age int
        constraint genprjprfimgjob_grwage_ck check (growth_age >= 1),
    growth_time numeric(10, 3)
        constraint genprjprfimgjob_grwtime_ck check (growth_time >= 0),
    polish_age int
        constraint genprjprfimgjob_polage_ck check (polish_age >= 1),
    polish_time numeric(10, 3)
        constraint genprjprfimgjob_poltime_ck check (polish_time >= 0),
    constraint genprjprfimgjob_pk primary key (id)
    ); 