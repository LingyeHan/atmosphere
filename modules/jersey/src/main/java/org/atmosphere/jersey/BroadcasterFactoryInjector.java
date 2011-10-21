/*
*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
*
* The contents of this file are subject to the terms of either the GNU
* General Public License Version 2 only ("GPL") or the Common Development
* and Distribution License("CDDL") (collectively, the "License").  You
* may not use this file except in compliance with the License. You can obtain
* a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
* or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
* language governing permissions and limitations under the License.
*
* When distributing the software, include this License Header Notice in each
* file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
* Sun designates this particular file as subject to the "Classpath" exception
* as provided by Sun in the GPL Version 2 section of the License file that
* accompanied this code.  If applicable, add the following below the License
* Header, with the fields enclosed by brackets [] replaced by your own
* identifying information: "Portions Copyrighted [year]
* [name of copyright owner]"
*
* Contributor(s):
*
* If you wish your version of this file to be governed by only the CDDL or
* only the GPL Version 2, indicate your decision by adding "[Contributor]
* elects to include this software in this distribution under the [CDDL or GPL
* Version 2] license."  If you don't indicate a single choice of license, a
* recipient has the option to distribute your version of this file under
* either the CDDL, the GPL Version 2 or to extend the choice of license to
* its licensees as provided above.  However, if you add GPL Version 2 code
* and therefore, elected the GPL Version 2 license, then the option applies
* only if the new code is made subject to such option by the copyright
* holder.
*/
package org.atmosphere.jersey;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;

import javax.ws.rs.core.Context;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Allow {@link org.atmosphere.cpr.BroadcasterFactory} injection via the {@link Context} annotation supported
 * by Jersey.
 *
 * @author Jeanfrancois Arcand
 * @author Paul Sandoz
 */
abstract class BroadcasterFactoryInjector extends BaseInjectableProvider {

    boolean isValidType(Type t) {
        return (t instanceof Class) && BroadcasterFactory.class.isAssignableFrom((Class) t);
    }

    public static final class PerRequest extends BroadcasterFactoryInjector {
        @Override
        public ComponentScope getScope() {
            return ComponentScope.PerRequest;
        }

        @Override
        public Injectable<BroadcasterFactory> getInjectable(ComponentContext ic, Context a, Type c) {
            if (!isValidType(c))
                return null;

            return new Injectable<BroadcasterFactory>() {
                @Override
                public BroadcasterFactory getValue() {
                    return getAtmosphereResource(AtmosphereResource.class, true).getAtmosphereConfig().getBroadcasterFactory();
                }
            };
        }
    }

    public static final class Singleton extends BroadcasterFactoryInjector {
        @Override
        public ComponentScope getScope() {
            return ComponentScope.Singleton;
        }

        @Override
        public Injectable<BroadcasterFactory> getInjectable(ComponentContext ic, Context a, Type c) {
            if (!isValidType(c))
                return null;

            return new Injectable<BroadcasterFactory>() {
                @Override
                public BroadcasterFactory getValue() {
                    return new BroadcasterFactoryProxy();
                }
            };
        }

        class BroadcasterFactoryProxy extends BroadcasterFactory {
            BroadcasterFactory _get() {
                return getAtmosphereResource(AtmosphereResource.class, true).getAtmosphereConfig().getBroadcasterFactory();
            }

            @Override
            public Broadcaster get() {
                return _get().get();
            }

            @Override
            public Broadcaster get(Class<? extends Broadcaster> c, Object id)  {
                return _get().get(c, id);
            }

            @Override
            public void destroy() {
                _get().destroy();
            }

            @Override
            public boolean add(Broadcaster b, Object id) {
                return _get().add(b, id);
            }

            @Override
            public boolean remove(Broadcaster b, Object id) {
                return _get().remove(b, id);
            }

            @Override
            public Broadcaster lookup(Class<? extends Broadcaster> c, Object id) {
                return _get().lookup(c, id);
            }

            @Override
            public Broadcaster lookup(Class<? extends Broadcaster> c, Object id, boolean createIfNull) {
                return _get().lookup(c, id, createIfNull);
            }

            @Override
            public void removeAllAtmosphereResource(AtmosphereResource<?, ?> r) {
                _get().removeAllAtmosphereResource(r);
            }

            @Override
            public Collection<Broadcaster> lookupAll() {
                return _get().lookupAll();
            }
        }
    }
}
