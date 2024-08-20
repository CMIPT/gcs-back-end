package edu.cmipt.gcs.controller.dao;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.core.toolkit.Assert;

import edu.cmipt.gcs.dao.UserMapper;
import edu.cmipt.gcs.pojo.UserPO;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        List<UserPO> userList = userMapper.selectList(null);
        Assert.isTrue(1 == userList.size(), "");
        userList.forEach(System.out::println);
    }
}
