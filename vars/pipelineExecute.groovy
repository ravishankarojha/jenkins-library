import com.sap.piper.GenerateDocumentation
import com.sap.piper.Utils

import groovy.transform.Field


@Field STEP_NAME = getClass().getName()

@Field Set PARAMETER_KEYS = [
    /** The url to the git repository of the pipeline to be loaded.*/
    'repoUrl',
    /** The branch of the git repository from which the pipeline should be checked out.*/
    'branch',
    /** The path to the Jenkinsfile, inside the repository, to be loaded.*/
    'path',
    /** The Jenkins credentials containing user and password needed to access a private git repository.*/
    'credentialsId'
]

/**
 * Loads and executes a pipeline from another git repository.
 * The idea is to set up a pipeline job in Jenkins that loads a minimal pipeline, which
 * in turn loads the shared library and then uses this step to load the actual pipeline.
 *
 * A centrally maintained pipeline script (Jenkinsfile) can be re-used by
 * several projects using `pipelineExecute` as outlined in the example below.
 */
@GenerateDocumentation
void call(Map parameters = [:]) {

    node() {

        def path

        handlePipelineStepErrors (stepName: 'pipelineExecute', stepParameters: parameters) {

            def utils = new Utils()

            // The coordinates of the pipeline script
            def repo = utils.getMandatoryParameter(parameters, 'repoUrl', null)
            def branch = utils.getMandatoryParameter(parameters, 'branch', 'master')

            path = utils.getMandatoryParameter(parameters, 'path', 'Jenkinsfile')

            // In case access to the repository containing the pipeline
            // script is restricted the credentialsId of the credentials used for
            // accessing the repository needs to be provided below. The corresponding
            // credentials needs to be configured in Jenkins accordingly.
            def credentialsId = utils.getMandatoryParameter(parameters, 'credentialsId', '')

            deleteDir()

            checkout([$class: 'GitSCM', branches: [[name: branch]],
                      doGenerateSubmoduleConfigurations: false,
                      extensions: [[$class: 'SparseCheckoutPaths',
                                    sparseCheckoutPaths: [[path: path]]
                                   ]],
                      submoduleCfg: [],
                      userRemoteConfigs: [[credentialsId: credentialsId,
                                           url: repo
                                          ]]
            ])

        }
        load path
    }
}
