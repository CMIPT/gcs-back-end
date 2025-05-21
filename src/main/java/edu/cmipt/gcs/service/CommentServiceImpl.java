package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.github.yulichang.toolkit.JoinWrappers;
import edu.cmipt.gcs.dao.CommentMapper;
import edu.cmipt.gcs.pojo.comment.CommentCountDTO;
import edu.cmipt.gcs.pojo.comment.CommentPO;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, CommentPO> implements CommentService{

    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @Override
    public CommentPO getById(Serializable id) {
        return super.getById(id);
    }

    @Override
    public boolean updateById(CommentPO comment) {
        return super.updateById(comment);
    }

    @Override
    public boolean save(CommentPO comment) {
        return super.save(comment);
    }

    @Override
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }
    @Override
    public void removeByActivityId(Serializable id) {
        super.remove(
                new QueryWrapper<CommentPO>()
                        .eq("activity_id", id));
    }

    @Override
    public Map<Long, Long> getCommentCountByActivityIds(List<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return Collections.emptyMap();
        }

        var wrapper =  JoinWrappers.lambda(CommentPO.class)
                .selectAs(CommentPO::getActivityId, CommentCountDTO::getActivityId)
                .selectCount(CommentPO::getId,CommentCountDTO::getCount)
                .in(CommentPO::getActivityId, activityIds)
                .groupBy(CommentPO::getActivityId);
        List<CommentCountDTO> list = commentMapper.selectJoinList(CommentCountDTO.class, wrapper);

        return list.stream().collect(Collectors.toMap(CommentCountDTO::getActivityId, CommentCountDTO::getCount));
    }

    @Override
    public void removeByActivityIds(List<Long> activityIds) {
        super.remove(
                new QueryWrapper<CommentPO>()
                        .in("activity_id", activityIds));
    }
}
