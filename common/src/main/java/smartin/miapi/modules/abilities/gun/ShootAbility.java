package smartin.miapi.modules.abilities.gun;

/*
public class ShootAbility implements CodecAbility<ShootAbility.GunAbilityContext> {
    public static ResourceLocation KEY = Miapi.id("gun_shot_single");

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return GunMagazineComponent.getBulletCount(itemStack) > 0;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.NONE;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity livingEntity) {
        return 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack gun = user.getItemInHand(hand);
        GunAbilityContext context = getSpecialContext(gun);

        if (context == null || GunMagazineComponent.getBulletCount(gun) <= 0) {
            return InteractionResultHolder.pass(gun);
        }

        if (!world.isClientSide) {
            performShooting(world, user, gun, context);
        }

        return InteractionResultHolder.success(gun);
    }

    @Override
    public Codec<GunAbilityContext> getCodec() {
        return GunAbilityContext.CODEC;
    }

    private void performShooting(Level world, Player user, ItemStack gun, GunAbilityContext context) {
        // Remove a bullet
        Optional<ItemStack> bullet = GunMagazineComponent.removeBullet(gun);
        if (bullet.isEmpty()) return;

        // Play shooting sound
        if (context.onShoot != null) {
            SoundEvent event = world.registryAccess().registry(Registries.SOUND_EVENT).get().get(context.onShoot);
            if (event != null) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), event, user.getSoundSource(), 1.0f, 1.0f);
            }
        }

        // Trigger animation
        if (context.shotAnim != null) {
            setAnimation(user, user.getUsedItemHand(), context.shotAnim);
        }

        GunMagazineComponent.shoot(world, user, context.hitscan);
    }

    public void setAnimation(Player p, InteractionHand hand, ResourceLocation animation) {
        if (p instanceof ServerPlayer player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null) {
                facet.set(animation.toString(), player, hand);
            }
        }
    }

    public void resetAnimation(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null)
                facet.reset(player);
        }
    }

    @Override
    public GunAbilityContext getDefaultContext() {
        return new GunAbilityContext(null, null, true);
    }

    public static class GunAbilityContext {
        public static final Codec<GunAbilityContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("on_shoot").forGetter(context -> Optional.ofNullable(context.onShoot)),
                ResourceLocation.CODEC.optionalFieldOf("shot_anim").forGetter(context -> Optional.ofNullable(context.shotAnim)),
                Miapi.FIXED_BOOL_CODEC.optionalFieldOf("hitscan", true).forGetter(context -> context.hitscan)
        ).apply(instance, (onShootOpt, shotAnimOpt, hitscan) -> new GunAbilityContext(
                onShootOpt.orElse(null),
                shotAnimOpt.orElse(null),
                hitscan
        )));

        public final ResourceLocation onShoot;
        public final ResourceLocation shotAnim;
        public final boolean hitscan;

        public GunAbilityContext(ResourceLocation onShoot, ResourceLocation shotAnim, boolean hitscan) {
            this.onShoot = onShoot;
            this.shotAnim = shotAnim;
            this.hitscan = hitscan;
        }

        public <T> T encode(com.mojang.serialization.DynamicOps<T> ops) {
            return CODEC.encodeStart(ops, this).result().orElseThrow(() -> new IllegalStateException("Failed to encode GunAbilityContext"));
        }
    }
}

 */
