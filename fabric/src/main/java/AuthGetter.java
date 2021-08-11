import com.mojang.authlib.Agent;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import java.net.Proxy;

public class AuthGetter {
    public static void main(String[] args) throws AuthenticationException {
        UserAuthentication auth = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1").createUserAuthentication(Agent.MINECRAFT);
        auth.setUsername(args[0]);
        auth.setPassword(args[1]);

        auth.logIn();

        System.out.println("Token: " + auth.getAuthenticatedToken());
        System.out.println("UUID: " + auth.getSelectedProfile().getId().toString());
    }
}
