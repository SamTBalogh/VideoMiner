package aiss.videominer.controller;

import aiss.videominer.exception.*;
import aiss.videominer.model.User;
import aiss.videominer.service.TokenService;
import aiss.videominer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="User", description="User management API")
@RestController
@RequestMapping("/videoMiner/v1")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    TokenService tokenService;

    // GET http://localhost:8080/videoMiner/v1/users
    @Operation( summary = "Retrieve a list of users",
            description = "Get a list of users with different options in paging, ordering and filtering. Only one of the filter parameters (`id`, `name`, `userLink`, `pictureLink`) may be present at the same time.<br /><br />" +
                    "Each filter parameter corresponds to one of the attributes of the User class. For example, `id` filters users by their unique identifier, `userLink` filters users by their user link and `pictureLink` filters users by their picture link.<br /><br />" +
                    "The parameter `page` indicates the page number of results to retrieve, while the `size` parameter specifies the number of results per page.<br />" +
                    "Pages are zero-indexed, so `page=0` returns the first page of results. If there is no result found the response will return empty.<br /><br />"+
                    "The `order` parameter specifies the ordering of the results. It accepts the name of the attribute by which you want to order the results. If descending order is desired, prefix the attribute with '-'. For example, 'name' for ascending order and '-name' for descending order.",
            tags = {"users", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema=@Schema(implementation = User.class)), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users")
    public List<User> findAll(@RequestHeader HttpHeaders header,
                              @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String id, @RequestParam(required = false) String name,
                              @RequestParam(required = false) String userLink, @RequestParam(required = false) String pictureLink,
                              @RequestParam(required = false) String order) throws TokenRequiredException, TokenNotValidException, BadRequestParameterField, BadRequestIdParameter {
        tokenService.validate(header);
        return userService.findAll(page, size, id, name, userLink, pictureLink, order);
    }

    // GET http://localhost:8080/videoMiner/v1/users/{id}
    @Operation( summary = "Retrieve a User by Id",
            description = "Get a User object by specifying its Id.",
            tags = {"users", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema=@Schema(implementation = User.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/{id}")
    public User findById(@PathVariable String id, @RequestHeader HttpHeaders header) throws UserNotFoundException, TokenNotValidException, TokenRequiredException {
        tokenService.validate(header);
        return userService.findById(id);
    }

    //GET http://localhost:8080/videoMiner/v1/videos/{videoId}/users
    @Operation( summary = "Retrieve the list of users of a Video",
            description = "Get a list of users associated with the video Id.",
            tags = {"users", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema=@Schema(implementation = User.class)), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/videos/{videoId}/users")
    public List<User> getAllCaptionsByVideo(@PathVariable("videoId") String videoId, @RequestHeader HttpHeaders header) throws VideoNotFoundException, TokenRequiredException, TokenNotValidException {
        tokenService.validate(header);
        return userService.findByVideo(videoId);
    }

    // PUT http://localhost:8080/videoMiner/v1/users/{id}
    @Operation( summary = "Update a User",
            description = "Update a User object by specifying its Id.<br >The id field cannot be modified.<br >The User data is passed in the body of the request in JSON format.",
            tags = {"captions", "put"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/users/{id}")
    public void update(@Valid @RequestBody User updatedUser, @PathVariable String id, @RequestHeader HttpHeaders header) throws UserNotFoundException, TokenNotValidException, TokenRequiredException {
        tokenService.validate(header);
        userService.update(id, updatedUser);
    }

    // DELETE http://localhost:8080/videoMiner/v1/users/{id}
    @Operation( summary = "Delete a User",
            description = "Delete a User object by specifying its Id.<br >Because of the relation with Comment in the model, the comment linked will be deleted too.",
            tags = {"users", "delete"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/users/{id}")
    public void delete(@Parameter(description = "Id of the user to be deleted") @PathVariable String id,
                       @RequestHeader HttpHeaders header) throws TokenRequiredException, TokenNotValidException, UserNotFoundException {
        tokenService.validate(header);
        userService.delete(id);
    }

}
