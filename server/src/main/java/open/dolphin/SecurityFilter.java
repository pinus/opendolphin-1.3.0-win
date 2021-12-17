package open.dolphin;

import open.dolphin.infomodel.InfoModel;
import open.dolphin.infomodel.UserModel;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.util.Encode;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Security Filter
 * Authorization Header pattern : "facilityId:username;password"
 * <p>
 * http://howtodoinjava.com/2013/07/25/jax-rs-2-0-resteasy-3-0-2-final-security-tutorial/
 *
 * @author pns
 */
@Provider
public class SecurityFilter implements ContainerRequestFilter {
    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Basic";
    private static final ServerResponse ACCESS_DENIED = new ServerResponse("Access denied", 401, new Headers<>());
    private static final ServerResponse ACCESS_FORBIDDEN = new ServerResponse("Access forbidden", 403, new Headers<>());

    private final static Map<String, Long> cachedUserMap = new HashMap<>();

    @PersistenceContext(unitName = "DolphinPU")
    private EntityManager em;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
        Class<?> clazz = methodInvoker.getResourceClass();
        Method method = methodInvoker.getMethod();

        // Method permissions override the permissions specified on the entire bean class
        AnnotatedElement target = method.isAnnotationPresent(PermitAll.class)
                || method.isAnnotationPresent(DenyAll.class)
                || method.isAnnotationPresent(RolesAllowed.class) ?
                method : clazz;

        if (!target.isAnnotationPresent(PermitAll.class)) {
            // Access denied for all
            if (target.isAnnotationPresent(DenyAll.class)) {
                requestContext.abortWith(ACCESS_FORBIDDEN);
                return;
            }

            // Verify user access
            if (target.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAnnotation = target.getAnnotation(RolesAllowed.class);
                Set<String> roles = new HashSet<>(Arrays.asList(rolesAnnotation.value()));

                // Get request headers
                final MultivaluedMap<String, String> headers = requestContext.getHeaders();

                // Fetch authorization header
                final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

                //  if no authorization information present; block access
                if (authorization == null || authorization.isEmpty()) {
                    requestContext.abortWith(ACCESS_DENIED);
                    return;
                }

                // Get encoded username and password
                final String encoded = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");

                // Decode username and password
                String usernameAndPassword = Encode.decode(encoded); // application/x-www-form-urlencoded

                // Split username and password tokens
                // Authorization header pattern is defined in ClientRequestFilter
                String[] split = usernameAndPassword.split(InfoModel.PASSWORD_SEPARATOR);
                final String userId = split[0];
                final String password = split[1];

                // is user valid?
                if (!isUserAllowed(userId, password, roles)) {
                    requestContext.abortWith(ACCESS_DENIED);
                }
            }
            // if nothing is annotated, regards as PermitAll
        }
    }

    /**
     * Check if user is authorized or not.
     *
     * @param userId
     * @param password
     * @param roles
     * @return
     */
    public boolean isUserAllowed(final String userId, final String password, final Set<String> roles) {

        //Step 1. Fetch user and password from database and match them with argument
        UserModel validUser = getValidUserModel(userId, password);
        if (validUser == null) {
            return false;
        }

        //Step 2. Verify user role
        return validUser.getRoles().stream().anyMatch(roleModel -> roles.contains(roleModel.getRole()));
    }

    /**
     * Fetch password-matched valid UserModel for authentication.
     *
     * @param userId
     * @param password
     * @return valid UserModel if present, return null if not.
     */
    public UserModel getValidUserModel(final String userId, final String password) {
        UserModel user = null;
        Long pk = cachedUserMap.get(userId);

        if (pk != null) {
            // try em.find using cache
            user = em.find(UserModel.class, pk);
        }
        // if cache missed; query to database
        if (pk == null || user == null) {

            List<UserModel> fetched = em.createQuery("select u from UserModel u where u.userId = :uid", UserModel.class)
                    .setParameter("uid", userId).getResultList();

            // user not found
            if (fetched.size() != 1) {
                return null;
            }
            // user found
            user = fetched.get(0);
            // cache pk
            cachedUserMap.put(user.getUserId(), user.getId());
        }
        // check password
        if (!user.getPassword().equals(password)) {
            return null;
        }

        return user;
    }
}
