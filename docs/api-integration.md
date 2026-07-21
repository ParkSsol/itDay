# API integration

```text
core/network/                 Retrofit, OkHttp, auth, common errors
feature/<name>/data/remote/   API interface
feature/<name>/data/model/    DTOs
feature/<name>/data/mapper/   DTO-domain mapping
feature/<name>/data/repository/ implementation
feature/<name>/domain/        contracts and business models
```

- Keep base URLs and secrets outside source control.
- Use one OkHttp/Retrofit setup unless an API needs different authentication or timeouts.
- Map transport errors at the repository boundary.
- Never expose Retrofit `Response`, DTOs, or HTTP handling to composables.
- Never log tokens, barcodes, location, or personal information.
- Use `suspend` for one-shot results and `Flow` for streams.
- Make retry explicit; do not automatically retry non-idempotent calls.

## Adding an endpoint

1. Define DTOs.
2. Add the Retrofit API function.
3. Map DTOs to domain models.
4. Update the domain repository contract only when needed.
5. Implement the repository method.
6. Add mapper, repository, and ViewModel tests where useful.
