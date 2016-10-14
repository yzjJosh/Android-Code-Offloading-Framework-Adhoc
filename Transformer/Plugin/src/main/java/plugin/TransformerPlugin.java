package plugin;

import org.gradle.api.Project;

import com.github.stephanenicolas.morpheus.AbstractMorpheusPlugin;

import javassist.build.IClassTransformer;
import transformer.RemoteMethodTransformer;

public class TransformerPlugin extends AbstractMorpheusPlugin {

    @Override
    protected Class<Project> getPluginExtension() {
        return null;
    }

    @Override
    protected String getExtension() {
        return null;
    }

    @Override
    public IClassTransformer[] getTransformers(Project project) {
        return new IClassTransformer[] { new RemoteMethodTransformer() };
    }

}
