package info.mikethomas.pushbullet.data;

/*-
 * #%L
 * Pushbullet
 * %%
 * Copyright (C) 2017 Mike Thomas <mikepthomas@outlook.com>
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import info.mikethomas.pushbullet.model.User;
import java.util.List;
import javax.annotation.Resource;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class UserRepositoryTest {

    @Resource
    private UserRepository userRepository;

    @Test
    public void testCreateUser() {
        // Given
        User user = new User();
        user.setUsername("bbcUser1");
        user.setAccessToken("anAccessToken");
        userRepository.save(user);

        // When
        User found = userRepository.findOne("bbcUser1");

        // Then
        assertThat(found.getAccessToken()).isEqualTo(user.getAccessToken());
    }

    @Test
    public void testFindUsers() {
        // Given
        User user1 = new User();
        user1.setUsername("bbcUser1");
        userRepository.save(user1);
        User user2 = new User();
        user2.setUsername("bbcUser2");
        userRepository.save(user2);

        // When
        List<User> found = userRepository.findAll();

        // Then
        assertThat(found).hasSize(2);
    }
}
