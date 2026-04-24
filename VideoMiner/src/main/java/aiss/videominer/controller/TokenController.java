package aiss.videominer.controller;

import aiss.videominer.exception.TokenManagementForbiddenException;
import aiss.videominer.exception.TokenNotFoundException;
import aiss.videominer.exception.TokenTtlOutOfRangeException;
import aiss.videominer.model.auth.TokenIssueRequest;
import aiss.videominer.model.auth.TokenIssueResponse;
import aiss.videominer.service.TokenService;
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
    TokenService tokenService;

    // POST http://localhost:8080/videoMiner/v1/token
    @Operation(summary = "Issue a new token",
            description = "Creates a new opaque token. Requires header X-Token-Management-Key. " +
                    "Returns the plain access token only once; VideoMiner stores only its hash.",
            tags = {"tokens", "post"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema(implementation = TokenIssueResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())})
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/token")
    public TokenIssueResponse issueToken(
            @RequestHeader(name = TokenService.MANAGEMENT_HEADER, required = false) String managementKey,
            @Valid @RequestBody(required = false) TokenIssueRequest request
    ) throws TokenManagementForbiddenException, TokenTtlOutOfRangeException {
        return tokenService.issueToken(managementKey, request);
    }

    // DELETE http://localhost:8080/videoMiner/v1/token/{id}
    @Operation(summary = "Revoke an existing token",
            description = "Revokes a token by its tokenId. Requires header X-Token-Management-Key.",
            tags = {"tokens", "delete"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/token/{id}")
    public void revokeToken(
            @PathVariable("id") String tokenId,
            @RequestHeader(name = TokenService.MANAGEMENT_HEADER, required = false) String managementKey
    ) throws TokenManagementForbiddenException, TokenNotFoundException {
        tokenService.revokeToken(tokenId, managementKey);
    }
}
