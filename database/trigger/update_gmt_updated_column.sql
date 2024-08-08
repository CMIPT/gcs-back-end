CREATE FUNCTION public.update_gmt_updated_column() 
RETURNS trigger 
LANGUAGE plpgsql AS $$
BEGIN 
    NEW.gmt_updated = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;
