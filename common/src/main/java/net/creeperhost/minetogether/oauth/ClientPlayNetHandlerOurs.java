package net.creeperhost.minetogether.oauth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;

public class ClientPlayNetHandlerOurs implements ClientGamePacketListener
{

    private final Connection networkManagerIn;

    public ClientPlayNetHandlerOurs(Connection networkManagerIn)
    {
        this.networkManagerIn = networkManagerIn;
    }

    @Override
    public void onDisconnect(Component reason)
    {
        ServerAuthTest.disconnected(ChatFormatting.stripFormatting(reason.getString()));
    }

    @Override
    public void handleDisconnect(ClientboundDisconnectPacket packetIn)
    {
        networkManagerIn.disconnect(packetIn.getReason());
    }

    @Override
    public Connection getConnection()
    {
        return networkManagerIn;
    }

    // NO-OP
    //@formatter:off
    @Override public void handleAddEntity(ClientboundAddEntityPacket clientboundAddEntityPacket) { }
    @Override public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket clientboundAddExperienceOrbPacket) { }
    @Override public void handleAddObjective(ClientboundSetObjectivePacket clientboundSetObjectivePacket) { }
    @Override public void handleAddPlayer(ClientboundAddPlayerPacket clientboundAddPlayerPacket) { }
    @Override public void handleAnimate(ClientboundAnimatePacket clientboundAnimatePacket) { }
    @Override public void handleAwardStats(ClientboundAwardStatsPacket clientboundAwardStatsPacket) { }
    @Override public void handleAddOrRemoveRecipes(ClientboundRecipePacket clientboundRecipePacket) { }
    @Override public void handleBlockDestruction(ClientboundBlockDestructionPacket clientboundBlockDestructionPacket) { }
    @Override public void handleOpenSignEditor(ClientboundOpenSignEditorPacket clientboundOpenSignEditorPacket) { }
    @Override public void handleBlockEntityData(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket) { }
    @Override public void handleBlockEvent(ClientboundBlockEventPacket clientboundBlockEventPacket) { }
    @Override public void handleBlockUpdate(ClientboundBlockUpdatePacket clientboundBlockUpdatePacket) { }
    @Override public void handleSystemChat(ClientboundSystemChatPacket clientboundSystemChatPacket) { }
    @Override public void handlePlayerChat(ClientboundPlayerChatPacket clientboundPlayerChatPacket) { }
    @Override public void handleChatPreview(ClientboundChatPreviewPacket clientboundChatPreviewPacket) { }
    @Override public void handleSetDisplayChatPreview(ClientboundSetDisplayChatPreviewPacket clientboundSetDisplayChatPreviewPacket) { }
    @Override public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket clientboundSectionBlocksUpdatePacket) { }
    @Override public void handleMapItemData(ClientboundMapItemDataPacket clientboundMapItemDataPacket) { }
    @Override public void handleContainerClose(ClientboundContainerClosePacket clientboundContainerClosePacket) { }
    @Override public void handleContainerContent(ClientboundContainerSetContentPacket clientboundContainerSetContentPacket) { }
    @Override public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket clientboundHorseScreenOpenPacket) { }
    @Override public void handleContainerSetData(ClientboundContainerSetDataPacket clientboundContainerSetDataPacket) { }
    @Override public void handleContainerSetSlot(ClientboundContainerSetSlotPacket clientboundContainerSetSlotPacket) { }
    @Override public void handleCustomPayload(ClientboundCustomPayloadPacket clientboundCustomPayloadPacket) { }
    @Override public void handleEntityEvent(ClientboundEntityEventPacket clientboundEntityEventPacket) { }
    @Override public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket clientboundSetEntityLinkPacket) { }
    @Override public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket clientboundSetPassengersPacket) { }
    @Override public void handleExplosion(ClientboundExplodePacket clientboundExplodePacket) { }
    @Override public void handleGameEvent(ClientboundGameEventPacket clientboundGameEventPacket) { }
    @Override public void handleKeepAlive(ClientboundKeepAlivePacket clientboundKeepAlivePacket) { }
    @Override public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket clientboundLevelChunkWithLightPacket) { }
    @Override public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket clientboundForgetLevelChunkPacket) { }
    @Override public void handleLevelEvent(ClientboundLevelEventPacket clientboundLevelEventPacket) { }
    @Override public void handleLogin(ClientboundLoginPacket clientboundLoginPacket) { }
    @Override public void handleMoveEntity(ClientboundMoveEntityPacket clientboundMoveEntityPacket) { }
    @Override public void handleMovePlayer(ClientboundPlayerPositionPacket clientboundPlayerPositionPacket) { }
    @Override public void handleParticleEvent(ClientboundLevelParticlesPacket clientboundLevelParticlesPacket) { }
    @Override public void handlePing(ClientboundPingPacket clientboundPingPacket) { }
    @Override public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket clientboundPlayerAbilitiesPacket) { }
    @Override public void handlePlayerInfo(ClientboundPlayerInfoPacket clientboundPlayerInfoPacket) { }
    @Override public void handleRemoveEntities(ClientboundRemoveEntitiesPacket clientboundRemoveEntitiesPacket) { }
    @Override public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket clientboundRemoveMobEffectPacket) { }
    @Override public void handleRespawn(ClientboundRespawnPacket clientboundRespawnPacket) { }
    @Override public void handleRotateMob(ClientboundRotateHeadPacket clientboundRotateHeadPacket) { }
    @Override public void handleSetCarriedItem(ClientboundSetCarriedItemPacket clientboundSetCarriedItemPacket) { }
    @Override public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket clientboundSetDisplayObjectivePacket) { }
    @Override public void handleSetEntityData(ClientboundSetEntityDataPacket clientboundSetEntityDataPacket) { }
    @Override public void handleSetEntityMotion(ClientboundSetEntityMotionPacket clientboundSetEntityMotionPacket) { }
    @Override public void handleSetEquipment(ClientboundSetEquipmentPacket clientboundSetEquipmentPacket) { }
    @Override public void handleSetExperience(ClientboundSetExperiencePacket clientboundSetExperiencePacket) { }
    @Override public void handleSetHealth(ClientboundSetHealthPacket clientboundSetHealthPacket) { }
    @Override public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket clientboundSetPlayerTeamPacket) { }
    @Override public void handleSetScore(ClientboundSetScorePacket clientboundSetScorePacket) { }
    @Override public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket clientboundSetDefaultSpawnPositionPacket) { }
    @Override public void handleSetTime(ClientboundSetTimePacket clientboundSetTimePacket) { }
    @Override public void handleSoundEvent(ClientboundSoundPacket clientboundSoundPacket) { }
    @Override public void handleSoundEntityEvent(ClientboundSoundEntityPacket clientboundSoundEntityPacket) { }
    @Override public void handleCustomSoundEvent(ClientboundCustomSoundPacket clientboundCustomSoundPacket) { }
    @Override public void handleTakeItemEntity(ClientboundTakeItemEntityPacket clientboundTakeItemEntityPacket) { }
    @Override public void handleTeleportEntity(ClientboundTeleportEntityPacket clientboundTeleportEntityPacket) { }
    @Override public void handleUpdateAttributes(ClientboundUpdateAttributesPacket clientboundUpdateAttributesPacket) { }
    @Override public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket clientboundUpdateMobEffectPacket) { }
    @Override public void handleUpdateTags(ClientboundUpdateTagsPacket clientboundUpdateTagsPacket) { }
    @Override public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket clientboundPlayerCombatEndPacket) { }
    @Override public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket clientboundPlayerCombatEnterPacket) { }
    @Override public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket clientboundPlayerCombatKillPacket) { }
    @Override public void handleChangeDifficulty(ClientboundChangeDifficultyPacket clientboundChangeDifficultyPacket) { }
    @Override public void handleSetCamera(ClientboundSetCameraPacket clientboundSetCameraPacket) { }
    @Override public void handleInitializeBorder(ClientboundInitializeBorderPacket clientboundInitializeBorderPacket) { }
    @Override public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket clientboundSetBorderLerpSizePacket) { }
    @Override public void handleSetBorderSize(ClientboundSetBorderSizePacket clientboundSetBorderSizePacket) { }
    @Override public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket clientboundSetBorderWarningDelayPacket) { }
    @Override public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket clientboundSetBorderWarningDistancePacket) { }
    @Override public void handleSetBorderCenter(ClientboundSetBorderCenterPacket clientboundSetBorderCenterPacket) { }
    @Override public void handleTabListCustomisation(ClientboundTabListPacket clientboundTabListPacket) { }
    @Override public void handleResourcePack(ClientboundResourcePackPacket clientboundResourcePackPacket) { }
    @Override public void handleBossUpdate(ClientboundBossEventPacket clientboundBossEventPacket) { }
    @Override public void handleItemCooldown(ClientboundCooldownPacket clientboundCooldownPacket) { }
    @Override public void handleMoveVehicle(ClientboundMoveVehiclePacket clientboundMoveVehiclePacket) { }
    @Override public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket clientboundUpdateAdvancementsPacket) { }
    @Override public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket clientboundSelectAdvancementsTabPacket) { }
    @Override public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket clientboundPlaceGhostRecipePacket) { }
    @Override public void handleCommands(ClientboundCommandsPacket clientboundCommandsPacket) { }
    @Override public void handleStopSoundEvent(ClientboundStopSoundPacket clientboundStopSoundPacket) { }
    @Override public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket clientboundCommandSuggestionsPacket) { }
    @Override public void handleUpdateRecipes(ClientboundUpdateRecipesPacket clientboundUpdateRecipesPacket) { }
    @Override public void handleLookAt(ClientboundPlayerLookAtPacket clientboundPlayerLookAtPacket) { }
    @Override public void handleTagQueryPacket(ClientboundTagQueryPacket clientboundTagQueryPacket) { }
    @Override public void handleLightUpdatePacket(ClientboundLightUpdatePacket clientboundLightUpdatePacket) { }
    @Override public void handleOpenBook(ClientboundOpenBookPacket clientboundOpenBookPacket) { }
    @Override public void handleOpenScreen(ClientboundOpenScreenPacket clientboundOpenScreenPacket) { }
    @Override public void handleMerchantOffers(ClientboundMerchantOffersPacket clientboundMerchantOffersPacket) { }
    @Override public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket clientboundSetChunkCacheRadiusPacket) { }
    @Override public void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket clientboundSetSimulationDistancePacket) { }
    @Override public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket clientboundSetChunkCacheCenterPacket) { }
    @Override public void handleBlockChangedAck(ClientboundBlockChangedAckPacket clientboundBlockChangedAckPacket) { }
    @Override public void setActionBarText(ClientboundSetActionBarTextPacket clientboundSetActionBarTextPacket) { }
    @Override public void setSubtitleText(ClientboundSetSubtitleTextPacket clientboundSetSubtitleTextPacket) { }
    @Override public void setTitleText(ClientboundSetTitleTextPacket clientboundSetTitleTextPacket) { }
    @Override public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket clientboundSetTitlesAnimationPacket) { }
    @Override public void handleTitlesClear(ClientboundClearTitlesPacket clientboundClearTitlesPacket) { }
    @Override public void handleServerData(ClientboundServerDataPacket clientboundServerDataPacket) { }
    //@formatter:on
}
