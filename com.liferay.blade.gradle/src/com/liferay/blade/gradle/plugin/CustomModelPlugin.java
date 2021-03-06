/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liferay.blade.gradle.plugin;

import com.liferay.blade.gradle.model.CustomModel;
import com.liferay.blade.gradle.model.DefaultModel;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.PublishArtifactSet;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskOutputs;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

/**
 * @author Gregory Amerson
 */
public class CustomModelPlugin implements Plugin<Project> {

	@Inject
	public CustomModelPlugin(ToolingModelBuilderRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void apply(Project project) {
		registry.register(new CustomModelBuilder());
	}

	private final ToolingModelBuilderRegistry registry;

	private static class CustomModelBuilder implements ToolingModelBuilder {

		@Override
		public Object buildAll(String modelName, Project project) {
			Set<String> pluginClassNames = new HashSet<>();

			for (Plugin<?> plugin : project.getPlugins()) {
				Class<?> clazz = plugin.getClass();

				pluginClassNames.add(clazz.getName());
			}

			Set<Task> buildTasks = project.getTasksByName("build", true);

			Set<Task> jarTasks = project.getTasksByName("jar", true);

			Set<Task> tasks = new HashSet<>();

			tasks.addAll(buildTasks);
			tasks.addAll(jarTasks);

			Set<File> outputFiles = new HashSet<>();

			for (Task task : tasks) {
				TaskOutputs outputs = task.getOutputs();

				FileCollection fileCollection = outputs.getFiles();

				Set<File> files = fileCollection.getFiles();

				outputFiles.addAll(files);
			}

			ConfigurationContainer configurations = project.getConfigurations();

			try {
				Configuration archivesConfiguration =
					configurations.getByName(
							Dependency.ARCHIVES_CONFIGURATION);

				PublishArtifactSet artifacts =
					archivesConfiguration.getArtifacts();

				FileCollection fileCollection = artifacts.getFiles();

				Set<File> files = fileCollection.getFiles();

				outputFiles.addAll(files);
			}
			catch (Exception e) {
			}

			return new DefaultModel(pluginClassNames, outputFiles);
		}

		@Override
		public boolean canBuild(String modelName) {
			return modelName.equals(CustomModel.class.getName());
		}

	}

}