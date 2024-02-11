package skyvangaurd.subscription.web;

import java.util.Optional;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import skyvangaurd.subscription.models.Subscription;
import skyvangaurd.subscription.models.User;
import skyvangaurd.subscription.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class UserController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	/**
	 *
	 * @param id
	 * @return Optional<User>
	 *
	 *         Provides the details of a user with the given id
	 *
	 */
	@GetMapping(value = "/users/{id}")
	public Optional<User> userDetails(@PathVariable("id") long id) {
		return retrieveUser(id);
	}

	/**
	 *
	 * @param userId
	 * @param subscriptionId
	 * @return Optional<Subscription>
	 *
	 *         Retrieves a subscription for a user
	 */
	@GetMapping(value = "/users/{userId}/subscriptions/{subscriptionId}")
	public Optional<Subscription> subscriptionDetailsByUser(@PathVariable("userId") Long userId,
			@PathVariable("subscriptionId") Long subscriptionId) {
		return userService.findByUserAndSubscriptionIds(userId, subscriptionId);
	}

	/**
	 *
	 * @param userId
	 * @return List<Subscription>
	 *
	 *         Provides a list of all subscriptions for a user
	 */
	@GetMapping(value = "/users/{userId}/subscriptions")
	public List<Subscription> subscriptionsByUser(@PathVariable("userId") Long userId) {
		return userService.findAllSubscriptions(userId);
	}

	/**
	 *
	 * @return List<User>
	 *
	 *         Provides a list of all users and associated subscriptions
	 */
	@GetMapping(value = "/users")
	public List<User> userList() {
		return userService.getAllusers();
	}

	/**
	 * 
	 * @param newUser
	 * @return entityWithLocation
	 * 
	 *         Registers a new user
	 */
	@PostMapping(value = "/users")
	public ResponseEntity<Void> resisterUser(@RequestBody User newUser) {
		User user = userService.registerUser(newUser);
		return entityWithLocation(user.getId());
	}

	/**
	 * 
	 * @param userId
	 * @param newSubscription
	 * @return entityWithLocation
	 * 
	 *         Adds a new subscription for an existing user
	 */
	@PostMapping(value = "/users/{id}/subscription")
	public ResponseEntity<Void> addSubscription(@PathVariable("id") Long userId,
			@RequestBody Subscription newSubscription) {
		userService.addSubscription(userId, newSubscription);
		return entityWithLocation(newSubscription.getId());
	}

	/**
	 * 
	 * @param userId
	 * @param subscriptionId
	 * @param updatedSubscriptionDetails
	 * @return entityWithLocation
	 * 
	 *         Updates an existing subscription for an existing user
	 */
	@PutMapping(value = "/users/{userId}/subscriptions/{subscriptionId}")
	public ResponseEntity<Void> updateSubscriptionForUser(
			@PathVariable("userId") Long userId,
			@PathVariable("subscriptionId") Long subscriptionId,
			@RequestBody Subscription updatedSubscriptionDetails) {
		Subscription updatedSubscription = userService.updateSubscriptionForUser(userId, subscriptionId,
				updatedSubscriptionDetails);
		return entityWithLocation(updatedSubscription);
	}

	/**
	 *
	 * @param userId
	 * @param subscriptionId
	 *
	 *         Removes an existing subscription for an existing user
	 */
	@DeleteMapping("/users/{userId}/subscriptions/{subscriptionId}")
	@ResponseStatus(HttpStatus.NO_CONTENT) // 204
	public void removeSubscription(@PathVariable("userId") Long userId,
			@PathVariable("subscriptionId") Long subscriptionId) {
		userService.deleteSubscriptionForUser(userId, subscriptionId);
	}

	/**
	 * Maps UnsupportedOperationException to a 501 Not Implemented HTTP status
	 * code.
	 */
	@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
	@ExceptionHandler({ UnsupportedOperationException.class })
	public void handleUnabletoReallocate(Exception ex) {
		logger.error("Exception is: ", ex);
	}

	/**
	 * Maps IllegalArgumentExceptions to a 404 Not Found HTTP status code.
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(IllegalArgumentException.class)
	public void handleNotFound(Exception ex) {
		logger.error("Exception is: ", ex);
	}

	/**
	 * Maps DataIntegrityViolationException to a 409 Conflict HTTP status code.
	 */
	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler({ DataIntegrityViolationException.class })
	public void handleAlreadyExists(Exception ex) {
		logger.error("Exception is: ", ex);
	}

	/**
	 * Finds the Account with the given id, throwing an IllegalArgumentException
	 * if there is no such Account.
	 */
	private Optional<User> retrieveUser(long userId) throws IllegalArgumentException {
		Optional<User> user = userService.findById(userId);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("No such user with id " + userId);
		}
		return user;
	}

	/**
	 * Return a response with the location of the new resource.
	 *
	 * Suppose we have just received an incoming URL of, say,
	 * http://localhost:8080/users and resourceId
	 * is "12345". Then the URL of the new resource will be
	 * http://localhost:8080/users/12345.
	 */
	private ResponseEntity<Void> entityWithLocation(Object resourceId) {

		// Determines URL of child resource based on the full URL of the given
		// request, appending the path info with the given resource Identifier
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequestUri()
				.path("/{resourceId}")
				.buildAndExpand(resourceId)
				.toUri();

		// Return an HttpEntity object - it will be used to build the
		// HttpServletResponse
		return ResponseEntity.created(location).build();
	}
}
