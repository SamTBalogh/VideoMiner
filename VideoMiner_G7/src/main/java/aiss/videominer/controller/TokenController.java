package aiss.videominer.controller;

import aiss.videominer.exception.IdCannotBeNull;
import aiss.videominer.model.Channel;
import aiss.videominer.model.Token;
import aiss.videominer.repository.TokenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name="Token", description="Token management API")
@RestController
@RequestMapping("/videoMiner/v1")
public class TokenController {

    @Autowired
    TokenRepository repository;

    // POST http://localhost:8080/videoMiner/api/v1/tokens
    @Operation( summary = "Insert a Token",
            description = "Add a Token object, the Token data is passed in the body of the request in JSON format.",
            tags = {"tokens", "post"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema=@Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/token")
    public Token addToken(@Valid @RequestBody Token token) throws IdCannotBeNull {
        if(token.getId() == null){
            throw new IdCannotBeNull();
        }
        repository.save(token);

        return token;
    }
}
