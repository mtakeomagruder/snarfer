--**********************************************************************************************************************
--* OBJECT Insert Trigger 
--**********************************************************************************************************************
CREATE or REPLACE FUNCTION object_trigger_insert() RETURNS trigger AS $$
    BEGIN
    insert into object (id, type) values (new.id, lower(tg_relname));
    perform journal_insert(new.id, 'insert', null);
    return new;
    END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER source_trigger_insert BEFORE INSERT ON source
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_insert();
    
CREATE TRIGGER source_url_trigger_insert BEFORE INSERT ON source_url
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_insert();
    
CREATE TRIGGER image_trigger_insert BEFORE INSERT ON image
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_insert();
    
CREATE TRIGGER article_trigger_insert BEFORE INSERT ON article
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_insert();

CREATE TRIGGER batch_trigger_insert BEFORE INSERT ON batch
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_insert();

CREATE TRIGGER project_trigger_insert BEFORE INSERT ON project
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_insert();

CREATE TRIGGER project_image_trigger_insert BEFORE INSERT ON project_image
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_insert();

CREATE TRIGGER genetic_profile_trigger_insert BEFORE INSERT ON genetic_profile
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_insert();

CREATE TRIGGER genetic_project_profile_trigger_insert BEFORE INSERT ON genetic_project_profile
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_insert();

CREATE TRIGGER genetic_project_profile_image_trigger_insert BEFORE INSERT ON genetic_project_profile_image
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_insert();

CREATE TRIGGER genetic_project_profile_image_lock_trigger_insert BEFORE INSERT ON genetic_project_profile_image_lock
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_insert();

CREATE TRIGGER genetic_project_profile_image_job_trigger_insert BEFORE INSERT ON genetic_project_profile_image_job
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_insert();

--**********************************************************************************************************************
--* OBJECT Delete Trigger 
--**********************************************************************************************************************
create or replace function object_trigger_delete() returns trigger as $$
    BEGIN
    delete from object 
    where id = old.id;

    return null; 
    END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER source_trigger_delete AFTER DELETE ON source
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_delete();

CREATE TRIGGER source_url_trigger_delete AFTER DELETE ON source_url
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_delete();

CREATE TRIGGER image_trigger_delete AFTER DELETE ON image
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_delete();

CREATE TRIGGER article_trigger_delete AFTER DELETE ON article
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_delete();
    
CREATE TRIGGER batch_trigger_delete AFTER DELETE ON batch
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_delete();

CREATE TRIGGER project_trigger_delete AFTER DELETE ON project
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_delete();

CREATE TRIGGER project_image_trigger_delete AFTER DELETE ON project_image
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_delete();

CREATE TRIGGER genetic_profile_trigger_delete AFTER DELETE ON genetic_profile
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_delete();

CREATE TRIGGER genetic_project_profile_trigger_delete AFTER DELETE ON genetic_project_profile
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_delete();

CREATE TRIGGER genetic_project_profile_image_trigger_delete AFTER DELETE ON genetic_project_profile_image
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_delete();

CREATE TRIGGER genetic_project_profile_image_lock_trigger_delete AFTER DELETE ON genetic_project_profile_image_lock
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_delete();

CREATE TRIGGER genetic_project_profile_image_job_trigger_delete AFTER DELETE ON genetic_project_profile_image_job
    FOR EACH ROW EXECUTE PROCEDURE object_trigger_delete();

--**********************************************************************************************************************
--* ARTICLE Delete Trigger
--*
--* Make sure that all orphaned images are deleted when an article is deleted.
--**********************************************************************************************************************
create or replace function article_trigger_delete() returns trigger as $$
declare    
    iCount int;    
begin
    select count(*) 
    into iCount
    from article
    where image_id = old.image_id;

    if iCount = 0 then
        delete from image
        where id = old.image_id;
    end if;

    return null; 
end;
$$ language 'plpgsql';

CREATE TRIGGER article_trigger_delete2 AFTER DELETE ON article
    FOR EACH ROW EXECUTE PROCEDURE article_trigger_delete();

--**********************************************************************************************************************
--* BATCH_ARTICLE Delete Trigger
--*
--* Make sure that all orphaned articles are deleted when a batch is deleted.
--**********************************************************************************************************************
create or replace function batch_article_trigger_delete() returns trigger as $$
declare    
    iCount int;    
begin
    select count(*) 
    into iCount
    from batch_article
    where article_id = old.article_id;

    if iCount = 0 then
        delete from article
        where id = old.article_id;
    end if;

    select count(*) 
    into iCount
    from batch_article
    where batch_id = old.batch_id;

    if iCount = 0 then
        delete from batch
        where id = old.batch_id;
    end if;

    return null; 
end;
$$ language 'plpgsql';

CREATE TRIGGER batch_article_trigger_delete2 AFTER DELETE ON batch_article
    FOR EACH ROW EXECUTE PROCEDURE batch_article_trigger_delete();