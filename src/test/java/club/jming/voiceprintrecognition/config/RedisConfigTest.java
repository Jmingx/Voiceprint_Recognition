package club.jming.voiceprintrecognition.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 测试redis能否存储对象
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class RedisConfigTest {
    @Autowired
    private RedisTemplate<Object,Object> template;

    @Test
    public void saveredis(){
        User user = new User();
        user.setAge(12);
        user.setName("王伟");
        template.opsForValue().set(user.getName(),user);
        User user1 = (User)template.opsForValue().get(user.getName());
        System.out.println(user1);
    }
}
