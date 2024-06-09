# Angular-web-ui

The project is a simple, not yet well UI prepared app handling DDM-Eval-PS Platform Coordinator API

## Build

Running app requires `npm` (version at least `18.13`) to build and start using commands:

```
npm install --legacy-peer-deps
npm start
```

By default app works on port 4200 (http://localhost:4200) and communicates with Platform Coordinator started on http://localhost:7000 which can be changed in property field of the class `RestService::baseUrl`

# TODO

- Configuration properties for e.g. Coordinator app address
- Prettier UI with custom components in forms
- Better error handling instead of snack bars
- Handle missing API endpoints:
  - `/coordinator/command/data/load/uri`
  - `/coordinator/command/data/partitioning`
