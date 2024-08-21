package edu.cmipt.gcs.dao;

import com.baomidou.mybatisplus.core.toolkit.Assert;

import edu.cmipt.gcs.pojo.user.UserPO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserMapperTest {

    @Autowired private UserMapper userMapper;

    @Test
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        List<UserPO> userList = userMapper.selectList(null);
        userList.forEach(System.out::println);
    }
}
