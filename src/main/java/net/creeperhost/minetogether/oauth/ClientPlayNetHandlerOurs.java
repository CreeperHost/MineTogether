package net.creeperhost.minetogether.oauth;

import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

@SuppressWarnings("NullableProblems")
public class ClientPlayNetHandlerOurs implements IClientPlayNetHandler {

    private final NetworkManager networkManagerIn;

    public ClientPlayNetHandlerOurs(NetworkManager networkManagerIn) {
        this.networkManagerIn = networkManagerIn;
    }

    @Override
    public void onDisconnect(ITextComponent reason) {
        ServerAuthTest.disconnected(TextFormatting.getTextWithoutFormattingCodes(reason.getString()));
    }

    @Override
    public void handleDisconnect(SDisconnectPacket packetIn) {
        networkManagerIn.closeChannel(packetIn.getReason());
    }

    @Override
    public NetworkManager getNetworkManager() {
        return networkManagerIn;
    }


    // NO-OP
    //@formatter:off
    @Override public void handleSpawnObject(SSpawnObjectPacket packetIn) { }
    @Override public void handleSpawnExperienceOrb(SSpawnExperienceOrbPacket packetIn) { }
    @Override public void handleSpawnMob(SSpawnMobPacket packetIn) { }
    @Override public void handleScoreboardObjective(SScoreboardObjectivePacket packetIn) { }
    @Override public void handleSpawnPainting(SSpawnPaintingPacket packetIn) { }
    @Override public void handleSpawnPlayer(SSpawnPlayerPacket packetIn) { }
    @Override public void handleAnimation(SAnimateHandPacket packetIn) { }
    @Override public void handleStatistics(SStatisticsPacket packetIn) { }
    @Override public void handleRecipeBook(SRecipeBookPacket packetIn) { }
    @Override public void handleBlockBreakAnim(SAnimateBlockBreakPacket packetIn) { }
    @Override public void handleSignEditorOpen(SOpenSignMenuPacket packetIn) { }
    @Override public void handleUpdateTileEntity(SUpdateTileEntityPacket packetIn) { }
    @Override public void handleBlockAction(SBlockActionPacket packetIn) { }
    @Override public void handleBlockChange(SChangeBlockPacket packetIn) { }
    @Override public void handleChat(SChatPacket packetIn) { }
    @Override public void handleMultiBlockChange(SMultiBlockChangePacket packetIn) { }
    @Override public void handleMaps(SMapDataPacket packetIn) { }
    @Override public void handleConfirmTransaction(SConfirmTransactionPacket packetIn) { }
    @Override public void handleCloseWindow(SCloseWindowPacket packetIn) { }
    @Override public void handleWindowItems(SWindowItemsPacket packetIn) { }
    @Override public void handleOpenHorseWindow(SOpenHorseWindowPacket packetIn) { }
    @Override public void handleWindowProperty(SWindowPropertyPacket packetIn) { }
    @Override public void handleSetSlot(SSetSlotPacket packetIn) { }
    @Override public void handleCustomPayload(SCustomPayloadPlayPacket packetIn) { }
    @Override public void handleEntityStatus(SEntityStatusPacket packetIn) { }
    @Override public void handleEntityAttach(SMountEntityPacket packetIn) { }
    @Override public void handleSetPassengers(SSetPassengersPacket packetIn) { }
    @Override public void handleExplosion(SExplosionPacket packetIn) { }
    @Override public void handleChangeGameState(SChangeGameStatePacket packetIn) { }
    @Override public void handleKeepAlive(SKeepAlivePacket packetIn) { }
    @Override public void handleChunkData(SChunkDataPacket packetIn) { }
    @Override public void processChunkUnload(SUnloadChunkPacket packetIn) { }
    @Override public void handleEffect(SPlaySoundEventPacket packetIn) { }
    @Override public void handleJoinGame(SJoinGamePacket packetIn) { }
    @Override public void handleEntityMovement(SEntityPacket packetIn) { }
    @Override public void handlePlayerPosLook(SPlayerPositionLookPacket packetIn) { }
    @Override public void handleParticles(SSpawnParticlePacket packetIn) { }
    @Override public void handlePlayerAbilities(SPlayerAbilitiesPacket packetIn) { }
    @Override public void handlePlayerListItem(SPlayerListItemPacket packetIn) { }
    @Override public void handleDestroyEntities(SDestroyEntitiesPacket packetIn) { }
    @Override public void handleRemoveEntityEffect(SRemoveEntityEffectPacket packetIn) { }
    @Override public void handleRespawn(SRespawnPacket packetIn) { }
    @Override public void handleEntityHeadLook(SEntityHeadLookPacket packetIn) { }
    @Override public void handleHeldItemChange(SHeldItemChangePacket packetIn) { }
    @Override public void handleDisplayObjective(SDisplayObjectivePacket packetIn) { }
    @Override public void handleEntityMetadata(SEntityMetadataPacket packetIn) { }
    @Override public void handleEntityVelocity(SEntityVelocityPacket packetIn) { }
    @Override public void handleEntityEquipment(SEntityEquipmentPacket packetIn) { }
    @Override public void handleSetExperience(SSetExperiencePacket packetIn) { }
    @Override public void handleUpdateHealth(SUpdateHealthPacket packetIn) { }
    @Override public void handleTeams(STeamsPacket packetIn) { }
    @Override public void handleUpdateScore(SUpdateScorePacket packetIn) { }
    @Override public void func_230488_a_(SWorldSpawnChangedPacket p_230488_1_) { }

    @Override public void handleTimeUpdate(SUpdateTimePacket packetIn) { }
    @Override public void handleSoundEffect(SPlaySoundEffectPacket packetIn) { }
    @Override public void handleSpawnMovingSoundEffect(SSpawnMovingSoundEffectPacket packetIn) { }
    @Override public void handleCustomSound(SPlaySoundPacket packetIn) { }
    @Override public void handleCollectItem(SCollectItemPacket packetIn) { }
    @Override public void handleEntityTeleport(SEntityTeleportPacket packetIn) { }
    @Override public void handleEntityProperties(SEntityPropertiesPacket packetIn) { }
    @Override public void handleEntityEffect(SPlayEntityEffectPacket packetIn) { }
    @Override public void handleTags(STagsListPacket packetIn) { }
    @Override public void handleCombatEvent(SCombatPacket packetIn) { }
    @Override public void handleServerDifficulty(SServerDifficultyPacket packetIn) { }
    @Override public void handleCamera(SCameraPacket packetIn) { }
    @Override public void handleWorldBorder(SWorldBorderPacket packetIn) { }
    @Override public void handleTitle(STitlePacket packetIn) { }
    @Override public void handlePlayerListHeaderFooter(SPlayerListHeaderFooterPacket packetIn) { }
    @Override public void handleResourcePack(SSendResourcePackPacket packetIn) { }
    @Override public void handleUpdateBossInfo(SUpdateBossInfoPacket packetIn) { }
    @Override public void handleCooldown(SCooldownPacket packetIn) { }
    @Override public void handleMoveVehicle(SMoveVehiclePacket packetIn) { }
    @Override public void handleAdvancementInfo(SAdvancementInfoPacket packetIn) { }
    @Override public void handleSelectAdvancementsTab(SSelectAdvancementsTabPacket packetIn) { }
    @Override public void handlePlaceGhostRecipe(SPlaceGhostRecipePacket packetIn) { }
    @Override public void handleCommandList(SCommandListPacket packetIn) { }
    @Override public void handleStopSound(SStopSoundPacket packetIn) { }
    @Override public void handleTabComplete(STabCompletePacket packetIn) { }
    @Override public void handleUpdateRecipes(SUpdateRecipesPacket packetIn) { }
    @Override public void handlePlayerLook(SPlayerLookPacket packetIn) { }
    @Override public void handleNBTQueryResponse(SQueryNBTResponsePacket packetIn) { }
    @Override public void handleUpdateLight(SUpdateLightPacket packetIn) { }
    @Override public void handleOpenBookPacket(SOpenBookWindowPacket packetIn) { }
    @Override public void handleOpenWindowPacket(SOpenWindowPacket packetIn) { }
    @Override public void handleMerchantOffers(SMerchantOffersPacket packetIn) { }
    @Override public void handleUpdateViewDistancePacket(SUpdateViewDistancePacket packetIn) { }
    @Override public void handleChunkPositionPacket(SUpdateChunkPositionPacket packetIn) { }
    @Override public void handleAcknowledgePlayerDigging(SPlayerDiggingPacket packetIn) { }
    //@formatter:on
}
