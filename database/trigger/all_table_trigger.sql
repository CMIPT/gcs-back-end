-- The trigger is added to the table.

-- The trigger of the t_repository table is added.
CREATE TRIGGER update_t_repository_gmt_updated
BEFORE UPDATE ON public.t_repository
FOR EACH ROW EXECUTE FUNCTION public.update_gmt_updated_column();

-- The trigger of the t_user table is added.
CREATE TRIGGER update_t_user_gmt_updated
BEFORE UPDATE ON public.t_user
FOR EACH ROW EXECUTE FUNCTION public.update_gmt_updated_column();

-- The trigger of the t_user_repository table is added.
CREATE TRIGGER update_t_user_repository_gmt_updated
BEFORE UPDATE ON public.t_user_repository
FOR EACH ROW EXECUTE FUNCTION public.update_gmt_updated_column();
