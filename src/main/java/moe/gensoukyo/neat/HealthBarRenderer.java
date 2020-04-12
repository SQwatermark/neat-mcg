package moe.gensoukyo.neat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class HealthBarRenderer {

    public HealthBarRenderer() {
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        //在mc未隐藏gui且neat绘制血槽的情况下，或者在设置了即便在f1也渲染血槽的情况下才会执行（渲染血槽）
        if ((NeatConfig.renderInF1 || Minecraft.isGuiEnabled()) && NeatConfig.draw) {
            //获取主视角实体（通常就是玩家本体）
            Entity cameraEntity = mc.getRenderViewEntity();
            assert cameraEntity != null;
            //获取照相机位置
            BlockPos renderingVector = cameraEntity.getPosition();
            //实例化一个视椎体并设置其位置
            Frustum frustum = new Frustum();
            float partialTicks = event.getPartialTicks();
            double viewX = cameraEntity.lastTickPosX + (cameraEntity.posX - cameraEntity.lastTickPosX) * (double)partialTicks;
            double viewY = cameraEntity.lastTickPosY + (cameraEntity.posY - cameraEntity.lastTickPosY) * (double)partialTicks;
            double viewZ = cameraEntity.lastTickPosZ + (cameraEntity.posZ - cameraEntity.lastTickPosZ) * (double)partialTicks;
            frustum.setPosition(viewX, viewY, viewZ);
            //如果仅在注视时才显示血槽，那么就会计算出玩家注视的实体，否则就会选择出所有在视线范围内的实体
            if (NeatConfig.showOnlyFocused) {
                Entity focused = getEntityLookedAt(mc.player);
                if (focused instanceof EntityLivingBase && focused.isEntityAlive()) {
                    this.renderHealthBar((EntityLivingBase)focused, partialTicks, cameraEntity);
                }
            } else {
                WorldClient client = mc.world;
                Set<Entity> entities = ReflectionHelper.getPrivateValue(WorldClient.class, client, new String[]{"entityList", "field_73032_d", "J"});
                Iterator<Entity> var15 = entities.iterator();

                while(true) {
                    Entity entity;
                    do {
                        do {
                            do {
                                do {
                                    do {
                                        if (!var15.hasNext()) {
                                            return;
                                        }

                                        entity = var15.next();
                                    } while(entity == null);
                                } while(!(entity instanceof EntityLivingBase));
                            } while(entity == mc.player);
                        } while(!entity.isInRangeToRender3d(renderingVector.getX(), renderingVector.getY(), renderingVector.getZ()));
                    } while(!entity.ignoreFrustumCheck && !frustum.isBoundingBoxInFrustum(entity.getEntityBoundingBox()));

                    if (entity.isEntityAlive() && entity.getRecursivePassengers().isEmpty()) {
                        this.renderHealthBar((EntityLivingBase)entity, partialTicks, cameraEntity);
                    }
                }
            }
        }
    }

    /**
     * 渲染血槽
     * @param passedEntity 目标实体
     * @param partialTicks ？
     * @param viewPoint 视角实体
     */
    public void renderHealthBar(EntityLivingBase passedEntity, float partialTicks, Entity viewPoint) {
        Stack<EntityLivingBase> ridingStack = new Stack<>();
        EntityLivingBase entity = passedEntity;
        ridingStack.push(passedEntity);

        while(entity.getRidingEntity() != null && entity.getRidingEntity() instanceof EntityLivingBase) {
            entity = (EntityLivingBase)entity.getRidingEntity();
            ridingStack.push(entity);
        }

        Minecraft mc = Minecraft.getMinecraft();
        float pastTranslate = 0.0F;

        while(true) {
            boolean boss;
            String entityID;
            double x;
            double y;
            double z;
            float scale;
            float maxHealth;
            float health;
            do {
                do {
                    do {
                        do {
                            float distance;
                            do {
                                do {
                                    do {
                                        if (ridingStack.isEmpty()) {
                                            return;
                                        }

                                        entity = ridingStack.pop();
                                        boss = !entity.isNonBoss();
                                        entityID = EntityList.getEntityString(entity);
                                    } while(NeatConfig.blacklist.contains(entityID));

                                    distance = passedEntity.getDistance(viewPoint);
                                } while(distance > (float)NeatConfig.maxDistance);
                            } while(!passedEntity.canEntityBeSeen(viewPoint));
                        } while(entity.isInvisible());
                    } while(!NeatConfig.showOnBosses && !boss);
                } while(!NeatConfig.showOnPlayers && entity instanceof EntityPlayer);

                x = passedEntity.lastTickPosX + (passedEntity.posX - passedEntity.lastTickPosX) * (double)partialTicks;
                y = passedEntity.lastTickPosY + (passedEntity.posY - passedEntity.lastTickPosY) * (double)partialTicks;
                z = passedEntity.lastTickPosZ + (passedEntity.posZ - passedEntity.lastTickPosZ) * (double)partialTicks;
                scale = 0.026666673F;
                maxHealth = entity.getMaxHealth();
                health = Math.min(maxHealth, entity.getHealth());
            } while(maxHealth <= 0.0F);

            float percent = (float)((int)(health / maxHealth * 100.0F));
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)(x - renderManager.viewerPosX), (float)(y - renderManager.viewerPosY + (double)passedEntity.height + NeatConfig.heightAbove), (float)(z - renderManager.viewerPosZ));
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-scale, -scale, scale);
            boolean lighting = GL11.glGetBoolean(2896);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            float padding = (float)NeatConfig.backgroundPadding;
            int bgHeight = NeatConfig.backgroundHeight;
            int barHeight = NeatConfig.barHeight;
            float size = (float)NeatConfig.plateSize;
            int r = 0;
            int g = 255;
            int b = 0;
            ItemStack stack = null;
            if (entity instanceof IMob) {
                r = 255;
                g = 0;
                EnumCreatureAttribute attr = entity.getCreatureAttribute();
                switch(attr) {
                    case ARTHROPOD:
                        stack = new ItemStack(Items.SPIDER_EYE);
                        break;
                    case UNDEAD:
                        stack = new ItemStack(Items.ROTTEN_FLESH);
                        break;
                    default:
                        stack = new ItemStack(Items.SKULL, 1, 4);
                }
            }

            if (boss) {
                stack = new ItemStack(Items.SKULL);
                size = (float)NeatConfig.plateSizeBoss;
                r = 128;
                g = 0;
                b = 128;
            }

            int armor = entity.getTotalArmorValue();
            boolean useHue = !NeatConfig.colorByType;
            float s;
            if (useHue) {
                s = Math.max(0.0F, health / maxHealth / 3.0F - 0.07F);
                Color color = Color.getHSBColor(s, 1.0F, 1.0F);
                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();
            }

            GlStateManager.translate(0.0F, pastTranslate, 0.0F);
            s = 0.5F;
            String name = I18n.format(entity.getDisplayName().getFormattedText());
            if (entity instanceof EntityLiving && entity.hasCustomName()) {
                name = TextFormatting.ITALIC + entity.getCustomNameTag();
            } else if (entity instanceof EntityVillager) {
                name = I18n.format("entity.Villager.name");
            }

            float namel = (float)mc.fontRenderer.getStringWidth(name) * s;
            if (namel + 20.0F > size * 2.0F) {
                size = namel / 2.0F + 10.0F;
            }

            float healthSize = size * (health / maxHealth);
            //绘制背景画布 TODO: 添加可配置的背景画布颜色，添加可以用材质绘制画布的选项
            if (NeatConfig.drawBackground) {
                buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(-size - padding, -bgHeight, 0.0D)                     .color(0, 0, 0, 64).endVertex();
                buffer.pos(-size - padding, (float)barHeight + padding, 0.0D) .color(0, 0, 0, 64).endVertex();
                buffer.pos(size + padding, (float)barHeight + padding, 0.0D)  .color(0, 0, 0, 64).endVertex();
                buffer.pos(size + padding, -bgHeight, 0.0D)                       .color(0, 0, 0, 64).endVertex();
                tessellator.draw();
            }

            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(-size, 0.0D, 0.0D)       .color(127, 127, 127, 127).endVertex();
            buffer.pos(-size, barHeight, 0.0D)      .color(127, 127, 127, 127).endVertex();
            buffer.pos(size, barHeight, 0.0D)       .color(127, 127, 127, 127).endVertex();
            buffer.pos(size, 0.0D, 0.0D)         .color(127, 127, 127, 127).endVertex();
            tessellator.draw();

            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(-size, 0.0D, 0.0D)                           .color(r, g, b, 127).endVertex();
            buffer.pos(-size, barHeight, 0.0D)                          .color(r, g, b, 127).endVertex();
            buffer.pos(healthSize * 2.0F - size, barHeight, 0.0D)   .color(r, g, b, 127).endVertex();
            buffer.pos(healthSize * 2.0F - size, 0.0D, 0.0D)     .color(r, g, b, 127).endVertex();
            tessellator.draw();

            GlStateManager.enableTexture2D();
            GlStateManager.pushMatrix();
            GlStateManager.translate(-size, -4.5F, 0.0F);
            GlStateManager.scale(s, s, s);
            if (NeatConfig.drawName) {
                mc.fontRenderer.drawString(name, 0, 0, 16777215);
            }
            GlStateManager.pushMatrix();
            float s1 = 0.75F;
            GlStateManager.scale(s1, s1, s1);
            int h = NeatConfig.hpTextHeight;
            String maxHpStr = TextFormatting.BOLD + "" + (double)Math.round((double)maxHealth * 100.0D) / 100.0D;
            String hpStr = "" + (double)Math.round((double)health * 100.0D) / 100.0D;
            String percStr = (int)percent + "%";
            if (maxHpStr.endsWith(".0")) {
                maxHpStr = maxHpStr.substring(0, maxHpStr.length() - 2);
            }

            if (hpStr.endsWith(".0")) {
                hpStr = hpStr.substring(0, hpStr.length() - 2);
            }

            if (NeatConfig.showCurrentHP) {
                mc.fontRenderer.drawString(hpStr, 2, h, 16777215);
            }

            if (NeatConfig.showMaxHP) {
                mc.fontRenderer.drawString(maxHpStr, (int)(size / (s * s1) * 2.0F) - 2 - mc.fontRenderer.getStringWidth(maxHpStr), h, 16777215);
            }

            if (NeatConfig.showPercentage) {
                mc.fontRenderer.drawString(percStr, (int)(size / (s * s1)) - mc.fontRenderer.getStringWidth(percStr) / 2, h, -1);
            }

            if (NeatConfig.enableDebugInfo && mc.gameSettings.showDebugInfo) {
                mc.fontRenderer.drawString("ID: \"" + entityID + "\"", 0, h + 16, -1);
            }

            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int off = 0;
            s1 = 0.5F;
            GlStateManager.scale(s1, s1, s1);
            GlStateManager.translate(size / (s * s1) * 2.0F - 16.0F, 0.0F, 0.0F);
            mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            if (stack != null && NeatConfig.showAttributes) {
                this.renderIcon(off, stack);
                off -= 16;
            }

            if (armor > 0 && NeatConfig.showArmor) {
                int ironArmor = armor % 5;
                int diamondArmor = armor / 5;
                if (!NeatConfig.groupArmor) {
                    ironArmor = armor;
                    diamondArmor = 0;
                }

                stack = new ItemStack(Items.IRON_CHESTPLATE);

                int i;
                for(i = 0; i < ironArmor; ++i) {
                    this.renderIcon(off, stack);
                    off -= 4;
                }

                stack = new ItemStack(Items.DIAMOND_CHESTPLATE);

                for(i = 0; i < diamondArmor; ++i) {
                    this.renderIcon(off, stack);
                    off -= 4;
                }
            }

            GlStateManager.popMatrix();
            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            if (lighting) {
                GlStateManager.enableLighting();
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
            pastTranslate -= (float)(bgHeight + barHeight) + padding;
        }
    }

    private void renderIcon(int vertexX, ItemStack stack) {
        try {
            IBakedModel iBakedModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
            TextureAtlasSprite textureAtlasSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(iBakedModel.getParticleTexture().getIconName());
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(vertexX, 16, 0.0D).tex(textureAtlasSprite.getMinU(), textureAtlasSprite.getMaxV()).endVertex();
            buffer.pos(vertexX + 16, 16, 0.0D).tex(textureAtlasSprite.getMaxU(), textureAtlasSprite.getMaxV()).endVertex();
            buffer.pos(vertexX + 16, 0, 0.0D).tex(textureAtlasSprite.getMaxU(), textureAtlasSprite.getMinV()).endVertex();
            buffer.pos(vertexX, 0, 0.0D).tex(textureAtlasSprite.getMinU(), textureAtlasSprite.getMinV()).endVertex();
            tessellator.draw();
        } catch (Exception ignored) {
        }

    }

    public static Entity getEntityLookedAt(Entity e) {
        Entity foundEntity = null;
        double distance = 32.0D;
        RayTraceResult pos = raycast(e, 32.0D);
        Vec3d positionVector = e.getPositionVector();
        if (e instanceof EntityPlayer) {
            positionVector = positionVector.add(0.0D, e.getEyeHeight(), 0.0D);
        }

        if (pos != null) {
            distance = pos.hitVec.distanceTo(positionVector);
        }

        Vec3d lookVector = e.getLookVec();
        Vec3d reachVector = positionVector.add(lookVector.x * 32.0D, lookVector.y * 32.0D, lookVector.z * 32.0D);
        Entity lookedEntity = null;
        List<Entity> entitiesInBoundingBox = e.getEntityWorld().getEntitiesWithinAABBExcludingEntity(e, e.getEntityBoundingBox().expand(lookVector.x * 32.0D, lookVector.y * 32.0D, lookVector.z * 32.0D).expand(1.0D, 1.0D, 1.0D));
        double minDistance = distance;
        Iterator<Entity> var14 = entitiesInBoundingBox.iterator();

        while(true) {
            do {
                do {
                    if (!var14.hasNext()) {
                        return foundEntity;
                    }

                    Entity entity = var14.next();
                    if (entity.canBeCollidedWith()) {
                        float collisionBorderSize = entity.getCollisionBorderSize();
                        AxisAlignedBB hitbox = entity.getEntityBoundingBox().expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
                        RayTraceResult interceptPosition = hitbox.calculateIntercept(positionVector, reachVector);
                        if (hitbox.contains(positionVector)) {
                            if (0.0D < minDistance || minDistance == 0.0D) {
                                lookedEntity = entity;
                                minDistance = 0.0D;
                            }
                        } else if (interceptPosition != null) {
                            double distanceToEntity = positionVector.distanceTo(interceptPosition.hitVec);
                            if (distanceToEntity < minDistance || minDistance == 0.0D) {
                                lookedEntity = entity;
                                minDistance = distanceToEntity;
                            }
                        }
                    }
                } while(lookedEntity == null);
            } while(minDistance >= distance && pos != null);

            foundEntity = lookedEntity;
        }
    }

    public static RayTraceResult raycast(Entity e, double len) {
        Vec3d vec = new Vec3d(e.posX, e.posY, e.posZ);
        if (e instanceof EntityPlayer) {
            vec = vec.add(new Vec3d(0.0D, e.getEyeHeight(), 0.0D));
        }

        Vec3d look = e.getLookVec();
        return raycast(e.getEntityWorld(), vec, look, len);
    }

    public static RayTraceResult raycast(World world, Vec3d origin, Vec3d ray, double len) {
        Vec3d end = origin.add(ray.normalize().scale(len));
        return world.rayTraceBlocks(origin, end);
    }
}
