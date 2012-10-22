package ch.cyberduck.core.cdn;

import ch.cyberduck.core.Protocol;

/**
 * @version $Id:$
 */
public class DistributionConfigurationFactory {

    public static DistributionConfiguration get(Protocol protocol) {
        switch(protocol.getType()) {

        }
//        return new CloudFrontDistributionConfiguration();
        return null;
    }
}
