# Compose Experiments

This simple Compose project is a way to play around with compose specifics and allow me to try various things in order to understand them better.

## Learnings about recomposition

1. composable level 1 **composes**
   * remember fields are evaluated and stored
   * composes all children
2. composable level 1 **REcomposes**:
   * remember fields are NOT REevaluated (the point of remember is to survive recomposition)
   * REcomposes all contained native Composables, even if their provided parameters have NOT changed
   * nested children Composables will recompose also ONLY IF the parameters they are provided change (not changing it could be achieved with remember or without)