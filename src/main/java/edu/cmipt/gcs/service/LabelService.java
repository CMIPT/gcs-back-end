package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.pojo.label.LabelPO;

public interface LabelService extends IService<LabelPO> {
    LabelPO getOneByNameAndRepositoryId(String labelName, Long repositoryId);

    LabelPO getOneByHexColorAndRepositoryId(String labelHexColor, Long repositoryId);

    void removeByRepositoryId(Long repositoryId);
}
