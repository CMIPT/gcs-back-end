package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import edu.cmipt.gcs.dao.RepositoryMapper;
import edu.cmipt.gcs.pojo.repository.RepositoryPO;

import org.springframework.stereotype.Service;

@Service
public class RepositoryServiceImpl extends ServiceImpl<RepositoryMapper, RepositoryPO>
        implements RepositoryService {}
