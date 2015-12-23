package ch.cyberduck.core.spectra;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3PathContainerService;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.SignatureException;
import java.util.EnumSet;
import java.util.List;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.GetBucketRequest;
import com.spectralogic.ds3client.commands.GetBucketResponse;
import com.spectralogic.ds3client.models.CommonPrefixes;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.networking.FailedRequestException;

public class SpectraObjectListService implements ListService {
    private static final Logger log = Logger.getLogger(SpectraObjectListService.class);

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final Preferences preferences
            = PreferencesFactory.get();

    private final SpectraSession session;

    public SpectraObjectListService(final SpectraSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<Path>();
            String prefix = StringUtils.EMPTY;
            if(!containerService.isContainer(directory)) {
                // Restricts the response to only contain results that begin with the
                // specified prefix. If you omit this optional argument, the value
                // of Prefix for your query will be the empty string.
                // In other words, the results will be not be restricted by prefix.
                prefix = containerService.getKey(directory);
                if(!prefix.endsWith(String.valueOf(Path.DELIMITER))) {
                    prefix += Path.DELIMITER;
                }
            }
            final Ds3Client client = new SpectraClientBuilder().wrap(session);
            final Path bucket = containerService.getContainer(directory);
            // Null if listing is complete
            String marker = null;
            do {
                // Get the list of objects from the bucket that you want to perform the bulk get with.
                final GetBucketResponse chunk = client.getBucket(
                        new GetBucketRequest(PathNormalizer.name(URIEncoder.encode(bucket.getName())))
                                .withDelimiter(String.valueOf(Path.DELIMITER))
                                .withMaxKeys(preferences.getInteger("s3.listing.chunksize"))
                                .withPrefix(prefix)
                                .withNextMarker(marker)
                );
                final List<Contents> objects = chunk.getResult().getContentsList();
                for(Contents object : objects) {
                    final String key = PathNormalizer.normalize(object.getKey());
                    if(new Path(bucket, key, EnumSet.of(Path.Type.directory, Path.Type.placeholder)).equals(directory)) {
                        continue;
                    }
                    final EnumSet<AbstractPath.Type> types = EnumSet.of(Path.Type.file);
                    final Path file = new Path(directory, PathNormalizer.name(key), types);
                    children.add(file);
                }
                final List<CommonPrefixes> prefixes = chunk.getResult().getCommonPrefixes();
                for(CommonPrefixes common : prefixes) {
                    if(common.getPrefix().equals(String.valueOf(Path.DELIMITER))) {
                        log.warn(String.format("Skipping prefix %s", common));
                        continue;
                    }
                    final String key = PathNormalizer.normalize(common.getPrefix());
                    if(new Path(bucket, key, EnumSet.of(Path.Type.directory, Path.Type.placeholder)).equals(directory)) {
                        continue;
                    }
                    final Path file = new Path(directory, PathNormalizer.name(key), EnumSet.of(Path.Type.directory, Path.Type.placeholder));
                    children.add(file);
                }
                marker = chunk.getResult().getNextMarker();
                listener.chunk(directory, children);
            }
            while(marker != null);
            return children;
        }
        catch(FailedRequestException e) {
            throw new SpectraExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(HttpResponseException e) {
            throw new HttpResponseExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(SignatureException e) {
            throw new BackgroundException(e);
        }
    }
}
