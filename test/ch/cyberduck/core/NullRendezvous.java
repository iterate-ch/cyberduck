package ch.cyberduck.core;

/**
 * @version $Id:$
 */
public class NullRendezvous extends AbstractRendezvous {
    @Override
    public void init() {
        //
    }

    @Override
    public void quit() {
        //
    }

    public static void register() {
        RendezvousFactory.addFactory(Factory.NATIVE_PLATFORM, new RendezvousFactory() {
            @Override
            protected Rendezvous create() {
                return new NullRendezvous();
            }
        });
    }
}
