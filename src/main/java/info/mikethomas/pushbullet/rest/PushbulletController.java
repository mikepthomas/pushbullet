package info.mikethomas.pushbullet.rest;

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

import info.mikethomas.pushbullet.data.UserRepository;
import info.mikethomas.pushbullet.model.Notification;
import info.mikethomas.pushbullet.model.User;
import io.swagger.annotations.ApiOperation;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
public class PushbulletController {

    @Autowired
    private UserRepository userRepository;

    @ApiOperation(
            value = "Create or update a user",
            notes = "If user doesn't exist it gets created, else it's updated",
            response = User.class
    )
    @PostMapping(
            value = "/rest/user",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity createUser(
            @RequestBody User user) {
        user.setCreationTime(new Date());
        try {
            userRepository.save(user);
        } catch (JpaSystemException ex) {
            return ResponseEntity.badRequest().body(ex);
        }
        return ResponseEntity.ok(user);
    }

    @ApiOperation(
            value = "Retrieve a list of all registered users",
            response = User.class, responseContainer = "List")
    @GetMapping(
            value = "/rest/users",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Iterable<User> getUsers() {
        return userRepository.findAll();
    }

    @ApiOperation(
            value = "Send notification to username",
            response = User.class
    )
    @PostMapping(
            value = "/rest/notification/{username}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity createNotification(
            @PathVariable String username,
            @RequestBody Notification notification) {
        // Get the user by username
        User user = userRepository.findOne(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Construct the request
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Token", user.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Notification> entity = new HttpEntity<>(notification, headers);

        // Send the request
        String url = "https://api.pushbullet.com/v2/pushes";
        try {
            ResponseEntity response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Notification.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                return response;
            }
        } catch (HttpClientErrorException ex) {
            return ResponseEntity.unprocessableEntity().body(ex.getMessage());
        }

        // Increment the count
        int count = user.getNumOfNotificationsPushed();
        user.setNumOfNotificationsPushed(count + 1);
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }
}
