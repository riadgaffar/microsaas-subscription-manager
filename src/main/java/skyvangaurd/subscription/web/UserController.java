package skyvangaurd.subscription.web;

import java.util.Optional;
import java.util.stream.Collectors;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import skyvangaurd.subscription.error.NotFoundException;
import skyvangaurd.subscription.models.Subscription;
import skyvangaurd.subscription.models.User;
import skyvangaurd.subscription.security.JwtUtil;
import skyvangaurd.subscription.serialization.AuthenticationResponse;
import skyvangaurd.subscription.serialization.AuthorityDto;
import skyvangaurd.subscription.serialization.SubscriptionDto;
import skyvangaurd.subscription.serialization.UserDetailsDto;
import skyvangaurd.subscription.serialization.UserRegistrationDto;
import skyvangaurd.subscription.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
public class UserController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private JwtUtil jwtUtil;

	@PostMapping(value = "/login")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody UserRegistrationDto userRegistrationDto) {
		try {
			SecurityContextHolder.getContext().setAuthentication(
					authenticationManager.authenticate(
							new UsernamePasswordAuthenticationToken(userRegistrationDto.email(), userRegistrationDto.password())));
		} catch (BadCredentialsException e) {
			return ResponseEntity.badRequest().body("Incorrect username or password");
		}

		String currentPrincipalName = SecurityContextHolder.getContext().getAuthentication().getName();
		final String jwt = jwtUtil.generateToken(currentPrincipalName);

		return ResponseEntity.ok(new AuthenticationResponse(jwt));
	}

	@GetMapping(value = "/authorities")
	public List<String> getAuthoritiesForUser(@RequestParam("email") String email) {
		return userService.getAuthoritiesForUser(email);
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
	public UserDetailsDto userDetails(@PathVariable("id") long id) {
		return retrieveUser(id);
	}

	/**
	 *
	 * @param userId
	 * @param subscriptionId
	 * @return SubscriptionDto
	 *
	 *         Retrieves a subscription for a user
	 */
	@GetMapping(value = "/users/{userId}/subscriptions/{subscriptionId}")
	public SubscriptionDto subscriptionDetailsByUser(@PathVariable("userId") Long userId,
			@PathVariable("subscriptionId") Long subscriptionId) {
		return retrieveSubscription(userId, subscriptionId);
	}

	/**
	 *
	 * @param userId
	 * @return List<SubscriptionDto>
	 *
	 *         Provides a list of all subscriptions for a user
	 */
	@GetMapping(value = "/users/{userId}/subscriptions")
	public List<SubscriptionDto> subscriptionsByUser(@PathVariable("userId") Long userId) {
		return convertSubscriptionsToDto(userService.findAllSubscriptions(userId));
	}

	/**
	 *
	 * @return List<User>
	 *
	 *         Provides a list of all users and associated subscriptions
	 */
	@GetMapping(value = "/users")
	public List<UserDetailsDto> userList() {
		return retrieveAllUsers(userService.getAllUsers());
	}

	/**
	 *
	 * @param newUser
	 * @return entityWithLocation
	 *
	 *         Registers a new user
	 */
	@PostMapping(value = "/users")
	public ResponseEntity<Void> registerUser(@RequestBody UserRegistrationDto registrationDto) {
		User user = userService.registerUser(registrationDto);
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
			@RequestBody SubscriptionDto newSubscription) {
		userService.addSubscription(userId, newSubscription);
		return entityWithLocation(newSubscription);
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
			@RequestBody SubscriptionDto updatedSubscriptionDetails) {
		Subscription updatedSubscription = userService.updateSubscriptionForUser(userId, subscriptionId,
				updatedSubscriptionDetails);
		return entityWithLocation(updatedSubscription);
	}

	/**
	 *
	 * @param userId
	 * @param subscriptionId
	 *
	 *                       Removes an existing subscription for an existing user
	 */
	@DeleteMapping("/users/{userId}/subscriptions/{subscriptionId}")
	@ResponseStatus(HttpStatus.NO_CONTENT) // 204
	public void removeSubscription(@PathVariable("userId") Long userId,
			@PathVariable("subscriptionId") Long subscriptionId) {
		userService.deleteSubscriptionForUser(userId, subscriptionId);
	}

	/**
	 * Maps UsernameNotFoundException to a 400 FORBIDDEN HTTP status code.
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({UsernameNotFoundException.class, IllegalArgumentException.class})
	public void handleBadRequest(Exception ex) {
		logger.error("Exception is: ", ex);
	}
	
	/**
	 * Maps AccessDeniedExceptions to a 403 FORBIDDEN HTTP status code.
	 */
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(AccessDeniedException.class)
	public void handleForbidden(Exception ex) {
		logger.error("Exception is: ", ex);
	}

	/**
	 * Maps NotFoundException to a 404 Not Found HTTP status code.
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NotFoundException.class)
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
	 * Maps UnsupportedOperationException to a 501 Not Implemented HTTP status
	 * code.
	 */
	@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
	@ExceptionHandler({ UnsupportedOperationException.class })
	public void handleUnabletoReallocate(Exception ex) {
		logger.error("Exception is: ", ex);
	}

	/**
	 * 
	 * Converts all Users retrieve from the database to a List of UserDetailsDto
	 */

	private List<UserDetailsDto> retrieveAllUsers(List<User> users) {
		List<UserDetailsDto> userListDtos = new ArrayList<>();

		for (User user : users) {
			userListDtos.add(convertToUserDto(user));
		}

		return userListDtos;
	}

	/**
	 * Finds the User with the given id, throwing an NotFoundException
	 * if there is no such User.
	 */
	private UserDetailsDto retrieveUser(long userId) throws NotFoundException {
		Optional<User> userOpt = userService.findById(userId);

		if (userOpt.isEmpty()) {
			throw new NotFoundException("No such user with id " + userId);
		}

		return convertToUserDto(userOpt.get());
	}

	/**
	 * 
	 * Converts User to UserDetailsDto
	 */
	private UserDetailsDto convertToUserDto(User user) {
		List<SubscriptionDto> subscriptionDtos = convertSubscriptionsToDto(user.getSubscriptions().stream().toList());

		List<AuthorityDto> authorityDtos = user.getAuthorities().stream()
				.map(authority -> new AuthorityDto(authority.getName()))
				.collect(Collectors.toList());

		UserDetailsDto UserDetailsDto = new UserDetailsDto(user.getId(), user.getEmail(), authorityDtos, subscriptionDtos);
		return UserDetailsDto;
	}

	// Helper method to convert subscriptions to SubscriptionDto list
	private List<SubscriptionDto> convertSubscriptionsToDto(List<Subscription> subscriptions) {
		return subscriptions.stream()
				.map(subscription -> new SubscriptionDto(subscription.getId(), subscription.getName(), subscription.getCost(),
						subscription.getRenewalDate()))
				.collect(Collectors.toList());
	}

	/**
	 * Finds the Subscription with the given id, for a given User with id, throwing
	 * an
	 * NotFoundException if there is no such Subscription.
	 */
	private SubscriptionDto retrieveSubscription(long userId, long subscriptionId)
			throws NotFoundException {

		return userService.findByUserAndSubscriptionIds(subscriptionId, userId)
            .map(subscription -> new SubscriptionDto(
                    subscription.getId(),
                    subscription.getName(),
                    subscription.getCost(),
                    subscription.getRenewalDate()))
            .orElseThrow(() -> new NotFoundException("Subscription not found for the given user ID: "
                    + userId + " and subscription ID: " + subscriptionId));
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
