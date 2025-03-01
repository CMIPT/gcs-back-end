package edu.cmipt.gcs.enumeration;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.user.UserPO;

public enum AddCollaboratorTypeEnum {
    ID,
    EMAIL,
    USERNAME;

    public QueryWrapper<UserPO> getQueryWrapper(String collaborator) {
        QueryWrapper<UserPO> wrapper = new QueryWrapper<>();
        switch (this) {
            case ID:
                if (collaborator == null) {
                    throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
                }
                try {
                    Long id = Long.valueOf(collaborator);
                    wrapper.eq("id", id);
                } catch (Exception e) {
                    throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
                }
                break;
            case USERNAME, EMAIL:
                if (collaborator == null) {
                    throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
                }
                wrapper.apply("LOWER(" + this.name().toLowerCase() + ") = LOWER({0})", collaborator);
                break;
        }
        return wrapper;
    }
}
