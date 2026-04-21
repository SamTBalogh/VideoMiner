package aiss.videominer.controller;

import aiss.videominer.exception.*;
import aiss.videominer.model.Caption;
import aiss.videominer.service.CaptionService;
import aiss.videominer.service.TokenService;
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

@Tag(name="Caption", description="Caption management API")
@RestController
@RequestMapping("/videoMiner/v1")
public class CaptionController {

    @Autowired
    CaptionService captionService;

    @Autowired
    TokenService tokenService;

    // GET http://localhost:8080/videoMiner/v1/captions
    @Operation( summary = "Retrieve a list of captions",
                description = "Get a list of captions with different options in paging, ordering and filtering. Only one of the filter parameters (`id`, `name`, `language`) may be present at the same time. <br /><br />" +
                        "Each filter parameter corresponds to one of the attributes of the Caption class. For example, `id` filters captions by their unique identifier, `name` filters captions by their name and `language` filters captions by their language. <br /><br />" +
                        "The parameter `page` indicates the page number of results to retrieve, while the `size` parameter specifies the number of results per page. <br />" +
                        "Pages are zero-indexed, so `page=0` returns the first page of results. If there is no result found the response will return empty. <br /><br />"+
                        "The `order` parameter specifies the ordering of the results. It accepts the name of the attribute by which you want to order the results. If descending order is desired, prefix the attribute with '-'. For example, 'name' for ascending order and '-name' for descending order.",
                tags = {"captions", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content (array = @ArraySchema(schema=@Schema(implementation = Caption.class)), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/captions")
    public List<Caption> findAll(@RequestHeader HttpHeaders header,
                                 @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String id, @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String language, @RequestParam(required = false) String order) throws TokenRequiredException, TokenNotValidException, BadRequestParameterField {
        tokenService.validate(header);
        return captionService.findAll(page, size, id, name, language, order);
    }

    // GET http://localhost:8080/videoMiner/v1/captions/{id}
    @Operation( summary = "Retrieve a Caption by Id",
            description = "Get a Caption object by specifying its Id.",
            tags = {"captions", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema=@Schema(implementation = Caption.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/captions/{id}")
    public Caption findById(@Parameter(description = "Id of the caption to be searched") @PathVariable String id, @RequestHeader HttpHeaders header) throws TokenRequiredException, TokenNotValidException, CaptionNotFoundException {
        tokenService.validate(header);
        return captionService.findById(id);
    }

    // GET http://localhost:8080/videoMiner/v1/videos/{videoId}/captions
    @Operation( summary = "Retrieve the list of captions of a Video",
            description = "Get a list of captions associated with the video Id.",
            tags = {"captions", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema=@Schema(implementation = Caption.class)), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/videos/{videoId}/captions")
    public List<Caption> getAllCaptionsByVideo(@Parameter (description = "The Id of the video which captions are to be retrieved") @PathVariable("videoId") String videoId,
                                               @RequestHeader HttpHeaders header) throws VideoNotFoundException, TokenRequiredException, TokenNotValidException {
        tokenService.validate(header);
        return captionService.findByVideo(videoId);
    }

    // POST http://localhost:8080/videoMiner/v1/videos/{videoId}/captions
    @Operation( summary = "Insert a Caption into the list of captions of a Video",
            description = "Add a Caption object into the list of captions associated with the video Id.<br >The Caption data is passed in the body of the request in JSON format.",
            tags = {"captions", "post"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema=@Schema(implementation = Caption.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/videos/{videoId}/captions")
    public List<Caption> create(@Parameter(description = "The ID of the video to which the caption is added") @PathVariable("videoId") String videoId,
                                @Valid @RequestBody Caption caption, @RequestHeader HttpHeaders header) throws VideoNotFoundException, TokenRequiredException, TokenNotValidException, IdCannotBeNull {
        tokenService.validate(header);
        return captionService.create(videoId, caption);
    }

    // PUT http://localhost:8080/videoMiner/v1/captions/{id}
    @Operation( summary = "Update a Caption",
            description = "Update a Caption object by specifying its Id and whose data is passed in the body of the request in JSON format.<br >The id field cannot be modified.<br >The Caption data is passed in the body of the request in JSON format.",
            tags = {"captions", "put"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/captions/{id}")
    public void update(@Valid @RequestBody Caption updatedCaption,
                       @Parameter(description = "Id of the caption to be updated") @PathVariable String id,
                       @RequestHeader HttpHeaders header) throws CaptionNotFoundException, TokenRequiredException, TokenNotValidException {
        tokenService.validate(header);
        captionService.update(id, updatedCaption);
    }

    // DELETE http://localhost:8080/videoMiner/v1/captions/{id}
    @Operation( summary = "Delete a Caption",
            description = "Delete a Caption object by specifying its Id.",
            tags = {"captions", "delete"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/captions/{id}")
    public void delete(@Parameter(description = "Id of the caption to be deleted") @PathVariable String id,
                       @RequestHeader HttpHeaders header) throws TokenRequiredException, TokenNotValidException, CaptionNotFoundException {
        tokenService.validate(header);
        captionService.delete(id);
    }
}
