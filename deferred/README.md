Code kept out of the compiled source set on purpose. Nothing under this directory is built.

- `src/.../compat/jei`, `compat/simulated`, `compat/sable/SableProxyImpl.java`, `compat/sable/fan`, `mixin/sable`,
  `mixin/simulated`: non-REI compat and Sable/Simulated integration, dropped for the first beta (the mods have no
  Fabric 26.2 builds). Kept so they can return.
- `src/.../circuits`: the client half of the circuit designer only (16 files, see the P8b entry at the end).
- `src/.../ponder/scenes/CircuitScenes.java`: the circuit designer's 13 ponder story boards, deferred with the
  client half. Their `PowerGridPonderScenes` call sites and the `CIRCUIT_COMPONENTS` ponder tag return with them.
- The six special item renderers are GONE from `deferred/`. Five came back as 26.2 `ItemModel` implementations in
  `client/model/` (`BoostingChipModel`, `ElectroZapperModel`, `DrillModel`, `ThermometerModel`, `MultimeterModel`,
  each with a nested `Unbaked` whose `MapCodec.unit` CODEC is registered by `collections/ModdedItemModels` through
  `AllModels.register`), and their five item definitions in `src/generated/.../items/` now say
  `{"model": {"type": "powergrid:model/<name>"}}`. **`SawItemRenderer` was dropped, not ported**: its `render`
  only re-rendered the unmodified base model, so a custom `ItemModel` for `portable_saw` would be a no-op that
  additionally forces `setAnimated()` and loses vanilla tint support; the item stays on `minecraft:model`.
  The first-person pose of `DrillItemRenderer.renderPlayerHand` and `SawItemRenderer.renderPlayerHand` (fired from
  NeoForge's `RenderHandEvent`) lives in `mixin/client/ItemInHandRendererMixin`, at the head of `submitArmWithItem`.
  `ThermometerItemRenderer` and `MultimeterItemRenderer` are back in the compiled source set with their `render`
  bodies removed: they now hold only the per-frame client state the models read (`needleAngle`, `getDialState`),
  their `clientTick`s, their two overlay text providers and the multimeter's world-space probe line.
  26.2 facts the rewrite rests on: a layer's quads live in `[0,1]` MODEL space and its `localTransform` is applied
  AFTER the display transform (`LayerRenderState.applyTransform` = `itemTransform.apply` then
  `pose.mulPose(localTransform)`), so every upstream pivot expressed in CENTERED item space becomes `0.5 + offset`
  — Fly's `PotatoCannonModel` pivots at `0.53125` for Create's `0.5f/16` and `SandPaperModel` brackets its
  first-person transform with `translate(0.5,0.5,0.5)` / `translate(-0.5,-0.5,-0.5)`. Fly reaches `localTransform`
  by a direct `getfield`, which needs its `create.classtweaker` widening; we use the public `setLocalTransform`
  instead so no access widener entry is required. Parts are baked with
  `ResolvedModel.bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY).getAll()` and pushed with
  `ItemModelRenderHelper.submitQuads`; `PartialModel` is not usable here because an `ItemModel` needs
  `List<BakedQuad>`, so the same model ids are resolved through `ResolvableModel.Resolver.markDependency`.
- SUSPECTED UPSTREAM DEFECT, kept verbatim for parity: `MultimeterItemRenderer.renderProbe` passes the probe
  endpoints RELATIVE to the player's rope-hold position into the `BlockAndTintGetter` overload of
  `HangingWireRenderer.renderFromPositions`, which then samples `getBrightness` at those relative coordinates —
  i.e. near world (0,0,0), not at the probe. The probe line is therefore lit by whatever is at the world origin.
  `fcf4c4d4` does exactly the same; do not "fix" it without deciding it deliberately.
- `upstream-fabric/`: pieces of upstream's disabled 1.20.1 Fabric module whose API is still valid (events, Team
  Reborn Energy bridge, registry helpers, REI plugin), lifted into the port as later phases need them.
- `upstream-forge/`: upstream NeoForge platform files read for behaviour while porting (events, energy, punch card
  inventory, circuit board model). Never compiled.
- The block-side render half is BACK in the source set: every block entity renderer, the seven Flywheel
  visuals, `collections/ModdedRenderLayers` (rewritten on 26.2 pipelines), `collections/ModdedModels`
  (connected textures) and a reduced `PowerGridClient` / `fabric/PowerGridClientFabric`. `fabric.mod.json`
  has its `client` entrypoint again. The reduced client entrypoint drops, until their own phases,
  `LevelKind.PONDER`, `ELECTRO_ZAPPER_RENDER_HANDLER`, `PlacementOverlay.init()`, the ponder plugin and the
  whole `clientTick` body; upstream `fcf4c4d4` still has them.
- The five entity renderers, the three particles and the worn-battery layer are BACK in the source set:
  `HangingWireRenderer`/`BlockWireRenderer`/`CordRenderer`/`StringLightCordRenderer`/`ZapProjectileRenderer`
  on `EntityRenderer<T, S>` with their own render states, `SparkParticle`/`MagnetizationParticle` on
  `SingleQuadParticle` and `ZapParticle` with its own `ZapParticleGroup` + `ZapParticleRenderState`, and
  `BatteryArmorLayer` rewritten as `BatteryArmorRenderer implements ArmorRenderer` (fabric-rendering-v1;
  the vanilla `RenderLayer` it used no longer takes part in armour rendering). Registration lives in
  `collections/{ModdedEntityRenders,ModdedParticleProviders}` and `BatteryArmorRenderer.register()`, all
  reached only from `PowerGridClient.initClient()`.
- `CordRenderer.renderPreview` and both `HangingWireRenderer.renderFromPositions` overloads survive with
  `(PoseStack.Pose | PoseStack, VertexConsumer | SubmitNodeCollector)` signatures for `WirePreview` and
  `MultimeterItemRenderer` to call when their phases land. They have NEVER been executed.
  `BlockWireRenderer.debugLine` went with the dead `getDebugLines()` branch.
- The Sable `rotateAround` wrappers around the cord plugs are gone: a `SuperByteBuffer` cannot pivot about an
  arbitrary point, and `getContainingClient` is a null stub, so the branch was dead. It returns with Sable.
- `ICustomParticleData{,WithSprite}` are deleted. A client type as the ERASED return or parameter type of an
  interface method loads that type when an implementing class links (proven: `ZapParticleData implements
  ICustomParticleDataWithFactory` with `ParticleProvider<T> getFactory()` threw `NoClassDefFoundError:
  net/minecraft/client/particle/ParticleProvider` from `ModdedParticles.<clinit>` on the dedicated server,
  while the sibling `Function<SpriteSet, ParticleProvider<T>> getMetaFactory()` linked fine because both
  client types sit inside generic arguments, which are never resolved). The provider table therefore lives
  in `ModdedParticleProviders`, not on the data classes. `PowerGridClientEvents` still calls the removed
  `ModdedParticles.registerFactories()` and must be pointed at `ModdedParticleProviders.register()` when it
  returns.
- The three menu screens are BACK in the source set, with `collections/ModIcons`, `utility/EditableScrollBox`,
  `customdisplay/TooltipWidget` and both punch-card buttons. 26.2 replaced `renderBg` with `extractBackground`,
  made `imageWidth`/`imageHeight` final (the size goes through the `AbstractSimiContainerScreen` constructor, so
  `setWindowSize` is gone), turned `GuiGraphics.pose()` into a 2D `Matrix3x2fStack` (`pushMatrix`/`rotateAbout`)
  and deleted `RenderSystem.setShaderColor`. `AllKeys.shiftDown()` is absent from Fly 6.0.9 — Fly's own
  `ScrollInput` uses `Minecraft.hasShiftDown()`. Screens register into `AllMenuScreens` from
  `collections/ModdedMenuScreens`, reached only from `PowerGridClient.initClient()`.
- The value boxes are BACK in the source set. Eleven client `ScrollValueBehaviour` halves and two
  `ScrollOptionBehaviour` halves live in `client/valuebox/` with their thirteen `ValueBoxTransform`s, wired from
  `collections/ModdedClientBehaviours` through `AllBlockEntityBehaviours.add`. `ScrollOptionBehaviour`'s
  constructor casts `Class.getEnumConstants()` to `INamedIconOptions[]`, so the enum passed to it MUST implement
  that client interface: `ClutchMode` and `DisplayModuleType` stay plain and a client-only mirror enum
  (`ModdedOptionBehaviours.{ClutchModeIcon,DisplayModuleIcon}`) is passed instead, mapped by ordinal.
  `SolarPanelBearingBlockScrollBehaviour` lost its own `BehaviourType`: the client half binds through
  `getBehaviour(ServerScrollValueBehaviour.TYPE)`, so a server half that overrides `getType()` can never be found.
- `utility/CustomValueSettingsScreen`, `electricity/transformer/TransformerWindingScreen`,
  `mixin/client/ValueSettingsScreenMixin` and `resources/powergrid.client.mixins.json` are BACK. The transformer's
  two winding call sites go through `TransformerWindingInteraction`, a common-side holder whose `Opener` is set
  from `PowerGridClient.initClient()`, so `TransformerBlock` never names a client class.
  `PowerGridClient.clientTick(Minecraft)` is registered from `PowerGridClientEvents` on
  `ClientTickEvents.END_CLIENT_TICK`, so both screens open.
- The client event wiring is BACK in the source set: `PowerGridClientEvents`, `utility/PlacementOverlay`,
  `electricity/wire/WirePreview`, `kinetics/generator/winding/WindingPreview`,
  `equipment/zapper/ElectroZapperRenderHandler` and
  `mixin/client/{BlueprintOverlayMixin,BlueprintOverlayRendererAccessor,ComplexEntityRaycastMixin}`, all reached
  only from `PowerGridClient.initClient()`. `ModPackets.PACKETS.registerS2CListener()` had NO caller before this
  phase, so no S2C packet ever reached a client; `PowerGridClientEvents.register()` now calls it.
  26.2 changes made on the way: `Options.hideGui` is gone (`Minecraft.gui.hud.isHidden()` replaces it);
  `RenderSystem.setShaderColor` is gone, so the overlay's fade alpha goes into the ARGB text colour;
  `DustParticleOptions` takes `(int rgb, float scale)`, not a `Vector3f`; and `BlueprintOverlayRenderer.tick`
  takes a `Minecraft` and no longer calls `Minecraft.getInstance()`, so the deactivation guard injects at HEAD.
  `mixin/client/RenderBuffersMixin` is dead on 26.2 and must never
  return: its `MultiBufferSource.immediateWithBuffers` target is gone and `defaultRequire: 1` would crash the
  client at mixin apply.
- The world overlay goes through fabric-api's `LevelRenderEvents.COLLECT_SUBMITS`, not a mixin of our own.
  Verified by bytecode: fapi injects it at RETURN of `LevelRenderer.submitFeatures(LevelRenderState,
  SubmitNodeCollector, Z)` and feeds it the very `PoseStack` that method constructs
  (`@ModifyExpressionValue` at `NEW PoseStack`) - the same identity pose Fly's own
  `LevelRendererMixin.afterSubmitParticles` captures with `@Local` a few instructions earlier, at
  `INVOKE finalizeGizmoCollection`. Camera position comes from `context.levelState().cameraRenderState.pos`
  and every renderer translates by `-camera` itself, exactly as Fly's overlays do.
- Dropped from `WirePreview` on the way back in: the `DEBUG_BLOCK_TRACING` branch (a dead `false` constant whose
  `BlockWireRenderer.debugLine` and `ModdedRenderLayers.getDebugLines()` are both gone) and the Sable sublevel
  rotation (`getContainingClient` is a null stub and a `SuperByteBuffer` cannot pivot about a point). Both
  return with Sable.
- `ElectroZapperRenderHandler`'s first-person recoil transform is REACHABLE again. Fly's `ItemInHandRendererMixin`
  `@WrapOperation`s the CALL to `ItemInHandRenderer.submitArmWithItem` and only ever consults its own two static
  handlers, so our `mixin/client/ItemInHandRendererMixin` injects at the HEAD of `submitArmWithItem` itself and
  cancels when `PowerGridClient.ELECTRO_ZAPPER_RENDER_HANDLER.onRenderPlayerHand(...)` returns true. Injecting
  inside the callee rather than wrapping the same call site means the two mixins cannot fight: if Fly's wrapper
  handles the stack the method is never entered, and otherwise ours runs. `onRenderPlayerHand` guards on
  `appliesTo(stack)` first, so non-zapper items fall through untouched. Its 26.2 signature is
  `(ItemStack, Minecraft, EntityRenderDispatcher, ItemInHandRenderer, PoseStack, SubmitNodeCollector, int light,
  float partialTick, InteractionHand, float equipProgress, float swingProgress)`, and it still takes a `PoseStack`,
  so `transformTool`/`transformHand` need no rewrite. Note also that Fly declares `playSound` **public** abstract
  where upstream had it protected. NEVER EXECUTED: this is bytecode-only until a client walk.
- `PlacementOverlay.init()` registers all seven of upstream's overlay text providers again, in upstream's order.
  The multimeter's probe line rides the same `LevelRenderEvents.COLLECT_SUBMITS` handler as `WirePreview.render`.
- `casingConnectivity` has no Create Fly equivalent (`CasingConnectivity` is absent from Fly 6.0.9), so the
  four upstream hooks on the conductive casing, the copper plating and its stairs and slab are dropped.
- The ponder plugin is BACK in the source set: 19 of the 20 files, 63 storyboards, the five custom instructions,
  both wire elements and `VoltageSource`. `PonderIndex.addPlugin(new PowerGridPonderPlugin())` and
  `LevelKind.PONDER = level -> level instanceof PonderLevel` are wired from `PowerGridClient.initClient()`.
  Still deferred with the circuit designer: `src/.../ponder/scenes/CircuitScenes.java` (918 lines, 13 storyboards),
  the `CIRCUIT_COMPONENTS` ponder tag in `PowerGridPonderTags` (its index icon is `ModdedBlocks.CIRCUIT_BOARD`,
  which does not exist yet) and its 24-entry `addToTag` block, and the 13 `assets/powergrid/ponder/circuit/*.nbt`
  schematics, which already ship. The lang keys for all of them are already in
  `src/generated/resources/assets/powergrid/lang/en_us.json` (13 `powergrid.ponder.circuit_*` scenes plus
  `powergrid.ponder.tag.circuit_components`), so P8 restores code only, never lang. The tag block as removed is
  in `scratchpad/pg/p7ponder/removed-circuit-tag.txt`; restore it verbatim except `AllItems.COPPER_NUGGET`,
  which is `Items.COPPER_NUGGET` on 26.2.
- `assets/powergrid/ponder/encased_fan.nbt` places one `powergrid:circuit_board` at [1,1,1]. `NbtUtils.readBlockState`
  turns an unknown block into AIR with no error (bytecode), so that scene is simply missing that block until P8;
  the schematic itself needs no change.
- The three menus are BACK in the source set. Fly registers menus in its OWN registry (`CreateRegistries.MENU_TYPE`), not vanilla's `BuiltInRegistries.MENU`, builds them on `MenuBase(com.zurrtum.create.foundation.gui.menu.MenuType<T>, int, Inventory, T)` and opens them with `com.zurrtum.create.foundation.gui.menu.MenuProvider.openHandledScreen`, which sends Fly's own `OpenScreenPacket`. Fly's `MenuBase` has NO `createOnClient` and no `RegistryFriendlyByteBuf` constructor: the client half reads the content holder in a `public static create(Minecraft, MenuType<H>, int, Inventory, Component, RegistryFriendlyByteBuf)` on the SCREEN, registered as `MyScreen::create` into `AllMenuScreens`. The vendored Registrate `MenuBuilder`/`MenuEntry` and `base/MenuOpener` are deleted with it, and `PunchCardMenu` is now concrete (`CONSTRUCTORS` was upstream's platform indirection).
- `resources/data/powergrid/{recipe,loot_table,advancement}`, `resources/assets/powergrid/blockstates` and
  `resources/assets/powergrid/models/item` entries for the circuit board, design table, schematic and incomplete
  circuit: data for the deferred circuit designer, parked with it so the datapack loads clean and every shipped
  item model has a registered item. `assets/powergrid/atlases/blocks.json` still stitches the two circuit-board
  sprites; harmless, and it goes back to being needed in that phase.
- **The circuit designer is COMPLETE (P8b + P8c, 2026-09-06).** All 98 circuit files are in the compiled source
  set. The client half went back as: `circuitboard/CircuitBoardRenderer` and `editor/CircuitDesignTableRenderer` on
  `SmartBlockEntityRenderer` (extract/submit, wired through the shim's `.renderer()`), the three design-table screens
  on `AbstractSimiContainerScreen` + `extractBackground`, the nine `gui/` widgets on `GuiGraphicsExtractor`, and
  `schematic/CircuitSchematicRender`.
  **The thirteen parked render bodies did NOT go back on the component classes, and that is a proven containment
  rule, not taste.** Putting `extractRender(..., List<ComponentDrawCall>)` on `IRenderedComponent` with the bodies on
  the nine component classes crashed the dedicated server at `Components.<clinit>` with
  `NoClassDefFoundError: net/minecraft/client/renderer/OrderedSubmitNodeCollector`. A method BODY is only lazy for
  what it *invokes*; the **bytecode verifier** loads any type it must check assignability for when the class is
  LINKED, and a lambda's synthetic body method counts. `@Environment(EnvType.CLIENT)` on the method would not have
  saved it either: Fabric strips the annotated method but leaves the unannotated synthetic `lambda$...` behind.
  (`Component.modelChanged` and `RotorBehaviour.tickAudio` are `@Environment(CLIENT)` with NO lambda, and those do
  strip cleanly - proven by four server boots.) The bodies therefore live in a client-only
  `circuits/client/ComponentRenderers`, a `Class -> ComponentRenderer` map walked up the superclass chain so
  `GaugeComponent`'s two subclasses resolve. `IRenderedComponent` keeps only `emitBaked()`. The same reasoning moved
  `ComponentFootprint`'s three gui methods into `circuits/client/FootprintRenderer`; the footprint gained
  `hasOutline()`, `hasItem()` and `getArrow()` so the renderer needs no private access.
  **The `powergrid:circuit_board` model is now data + code, not code alone.** `blockstates/circuit_board.json` points
  at the real `powergrid:block/circuit_board` plate model and `ModdedModels` wraps the block with
  `AllModels.register(CIRCUIT_BOARD, CircuitBoardModel.of())` - Fly's `WrapperBlockStateModel`, whose
  `addPartsWithInfo(BlockAndTintGetter, BlockPos, ...)` is the only 26.2 hook that can read a block entity at collect
  time (vanilla `BlockStateModel.collectParts` takes only a `RandomSource`). Components bake once per resource reload
  through `SimpleModelWrapper.bake` behind `resolveDependencies`/`markDependency`; traces and pads bake per board with
  `FaceBakery.bakeQuad(ModelBaker$Interner, ...)`; `CircuitBoardModelQuads` now caches the finished
  `List<BlockStateModelPart>` per block state and `Component.modelChanged` still nulls it.
  **One upstream behaviour is not expressible on 26.2**: a destroyed component used to be tinted `(64,64,64)` through
  NeoForge's `QuadTransformers.applyingColor`. 26.2's `BakedQuad` record has no colour field at all, so destroyed
  components render at full brightness.
- `src/.../compat/jei`: the JEI plugin. Not ported - the devpack and the four shipped ports run REI, and JEI breaks
  multiplayer joins on Create Fly (see PROJECT.md).
- `upstream-fabric/compat/rei`: upstream's REI plugin. **Not ported, deliberately.** REI 26.2.820 does exist and the
  devpack ships it, but none of the four shipped ports carries REI compat on 26.2, and upstream's three categories
  (`CircuitAssemblyCategory`, `CircuitDesignCategory`, `MagnetizingCategory`) all extend
  `com.simibubi.create.compat.rei.category.CreateRecipeCategory` - and **Create Fly 6.0.9 ships no recipe-viewer
  integration at all** (a constant-pool sweep of the jar finds no `compat/rei` package and no `CreateRecipeCategory`).
  Porting it means writing the whole category framework from scratch, not adapting a plugin.
- `src/.../mixin/ArmBlockEntityMixin`: **a confirmed no-op upstream** - both branches of its
  `handleCenteredProcessingOnAllItems` callback return `TransportedResult.doNothing()` - and its `target=` still names
  the 1.20.1 `com.simibubi.create` path, which would fail to apply on Fly. Left parked.
- `src/.../electricity/sim/solver/NativeMNA.java`: the native MNA solver backend. This build ships no `.so`/`.dll`/
  `.dylib`, and nothing called `tryLoad()`, so `isSupported()` could only ever be false. The `NATIVE` constant is
  gone from `CSolver.SolverBackend` with it, so `solverBackend` now accepts only `JAVA` and the error-and-revert
  path in `GlobalElectricNetworks.configsReloaded` is unreachable.
- DROPPED, cosmetic: the Electro-Zapper's two-handed crossbow hold. Upstream implements NeoForge's
  `CustomArmPoseItem.getArmPose` and returns `ArmPose.CROSSBOW_HOLD` when the player is not swinging. Create Fly
  6.0.9 ships no equivalent (`com/zurrtum/create/foundation/item/` has no arm-pose type and the whole jar contains
  no `ArmPose` string), and Fly's own Blockzapper and Worldshaper are held as ordinary items, so the zapper now
  matches every other zapper on this platform. A client-only mixin on the humanoid arm-pose selection could
  restore it; nothing else changes (no hitbox, damage, aim, cooldown or first-person render difference).
- DROPPED, cosmetic: the creative tab's three-pass ordering. Upstream fills `IS_ITEM_3D_PREDICATE` from a client
  `model.isGui3d()` check so the tab lists 3D items, then blocks, then flat items. The 26.2 client jar has no
  `BakedModel` and no `gui3d`, so the predicate has no equivalent; the field and the two-pass split are gone and
  the tab lists blocks then items. The exclusion list itself is at upstream parity.
- INHERITED DEAD CODE, kept verbatim for parity: `MultimeterItem.getMeasurement` prefers cached terminal voltages
  under the `PosV` and `NegV` keys of the item's `ModeData` compound, but nothing anywhere writes either key -
  `fcf4c4d4` has the same two read sites and no writer, so the item always falls through to the live node lookup.
  Do not invent a writer; do not re-discover it.

- INHERITED DEAD CODE, kept verbatim for parity: `src/.../kinetics/base/GeneratorBlockEntity.java` is abstract with
  three abstract methods and has no subclass and no reference anywhere in the mod - a constant-pool sweep of every
  `org/patryk3211` class in the built jar names it exactly once, in its own class file. `fcf4c4d4` is the same: the
  file exists there and nothing extends it. Its absence from a future parity diff would not be a regression.
