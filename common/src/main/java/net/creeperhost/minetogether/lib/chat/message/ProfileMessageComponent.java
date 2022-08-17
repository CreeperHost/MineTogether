package net.creeperhost.minetogether.lib.chat.message;

import net.creeperhost.minetogether.lib.chat.profile.Profile;

/**
 * Created by covers1624 on 13/7/22.
 */
public class ProfileMessageComponent extends MessageComponent {

    public final Profile profile;

    private String displayName;

    public ProfileMessageComponent(Profile profile) {
        this.profile = profile;
        displayName = profile.getDisplayName();
        profile.addListener(this, ProfileMessageComponent::onProfileUpdate);
    }

    private void onProfileUpdate(Profile profile) {
        assert this.profile == profile : "Profile update got a different profile???";

        String newName = profile.getDisplayName();
        if (!newName.equals(displayName)) {
            displayName = newName;
            fire(this);
        }
    }

    @Override
    public String getMessage() {
        return displayName;
    }
}
