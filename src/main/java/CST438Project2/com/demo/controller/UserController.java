
package CST438Project2.com.demo.controller;

import CST438Project2.com.demo.model.User;
import CST438Project2.com.demo.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Handles administrator operations for users.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;

    /**
     * Creates the controller with its user repository.
     *
     * @param userRepository repository used to store users
     */
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a user.
     *
     * @param user user details to save
     * @return the created user
     */
    @PostMapping
    @Operation(summary = "Create a user")
    @ApiResponse(responseCode = "201", description = "User created")
    public ResponseEntity<User> createUser(@RequestBody User user) {

        User createdUser = userRepository.save(user);

        return ResponseEntity
                .created(URI.create("/api/v1/users/" + createdUser.getId()))
                .body(createdUser);
    }

    /**
     * Returns all users.
     *
     * @return all users in the database
     */
    @GetMapping
    @Operation(summary = "List users")
    @ApiResponse(responseCode = "200", description = "Users returned")
    public ResponseEntity<List<User>> getAllUsers() {

        List<User> users = userRepository.findAll();

        return ResponseEntity.ok(users);
    }

    /**
     * Finds one user by id.
     *
     * @param userId user id to find
     * @return the user, or a not-found response
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get a user by id")
    @ApiResponse(responseCode = "200", description = "User returned")
    public ResponseEntity<User> getUserById(
            @PathVariable Long userId) {

        Optional<User> user = userRepository.findById(userId);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Updates an existing user.
     *
     * @param userId user id to update
     * @param user new user values
     * @return the updated user, or a not-found response
     */
    @PatchMapping("/{userId}")
    @Operation(summary = "Update a user")
    @ApiResponse(responseCode = "200", description = "User updated")
    public ResponseEntity<User> updateUser(
            @PathVariable Long userId,
            @RequestBody User user) {

        Optional<User> updatedUser = userRepository.update(
                userId,
                user.getDisplayName(),
                user.getEmail(),
                user.getRole()
        );

        if (updatedUser.isPresent()) {
            return ResponseEntity.ok(updatedUser.get());
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Deletes a user by id.
     *
     * @param userId user id to delete
     * @return no content when deleted, or a not-found response
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete a user")
    @ApiResponse(responseCode = "204", description = "User deleted")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId) {

        boolean deleted = userRepository.deleteById(userId);

        if (deleted) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
