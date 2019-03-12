import org.sonatype.nexus.security.user.UserNotFoundException


def name = "${SHATHEL_ENV_NEXUS_INSTALL_NAME}"

def repoDockerHostedName = "${name}-docker-snapshots"
def repoDockerReleasesName = "${name}-docker-releases"
def repoDockerProxyName = "${name}-docker-hub-proxy"
def repoDockerGroupName = "${name}-docker-group"

def repoMvnHostedName = "${name}-mvn-snapshots"
def repoMvnReleasesName = "${name}-mvn-releases"
def repoMvnProxy1Name = "${name}-mvn-central-proxy"
def repoMvnProxy2Name = "${name}-mvn-jcenter-proxy"
def repoMvnGroupName = "${name}-mvn-group"

def allRepoNames = [repoDockerHostedName,
                 repoDockerReleasesName,
                 repoDockerProxyName,
                 repoDockerGroupName].collect { "docker-$it".toString() } +
        [repoMvnHostedName,
         repoMvnReleasesName,
         repoMvnProxy1Name,
         repoMvnProxy2Name,
         repoMvnGroupName].collect { "maven2-$it".toString() }

def snapshotRepoNames = [repoDockerHostedName].collect { "docker-$it".toString() } +
        [repoMvnHostedName].collect { "maven2-$it".toString() }

def releaseRepoNames = [repoDockerReleasesName].collect { "docker-$it".toString() } +
        [repoMvnReleasesName].collect { "maven2-$it".toString() }


def usersExist = false
try {
    if (security.getSecuritySystem().getUser('dev') && security.getSecuritySystem().getUser('ci')) usersExist = true
} catch (UserNotFoundException ex) {
    log.info('Will create users')
}
import groovy.json.JsonOutput

security.setAnonymousAccess(false)
log.info('Anonymous access disabled')
if (!usersExist) {/*To create new admin user you will have to assign nx-admin role*/
/*Create a new role that allows a user same access as anonymous and adds healtchcheck access*/
    def devPrivileges = ['nx-healthcheck-read', 'nx-healthcheck-summary-read', 'nx-search-read'] +
                allRepoNames.collect{"nx-repository-view-$it-read".toString()} +
                allRepoNames.collect{"nx-repository-view-$it-browse".toString()}
    def anoRole = []
/* add roles that uses the built in nx-anonymous role as a basis and adds more privileges*/
    security.addRole('developer', 'Developer', 'User with privileges to allow read access to repo content and healtcheck', devPrivileges, anoRole)
    log.info('Role developer created')
/* use the new role to create a user*/
    def devRoles = ['developer']
    def johnDoe = security.addUser('dev', 'dev', 'ops', '${SHATHEL_ENV_NEXUS_DEV_MAIL"}', true, '${SHATHEL_ENV_NEXUS_DEV_PASS}', devRoles)
    log.info('User developer created')
/*Create new role that allows deployment and create a user to be used on a CI server*/
/* privileges with pattern * to allow any format, browse and read are already part of nx-anonymous*/
    def depPrivileges = snapshotRepoNames.collect{"nx-repository-view-$it-add".toString()} +
            snapshotRepoNames.collect{"nx-repository-view-$it-edit".toString()}
    def roles = ['developer']
/* add roles that uses the developer role as a basis and adds more privileges*/
    security.addRole('deployer', 'Deployer', 'User with privileges to allow deployment snap repositories', depPrivileges, roles)
    log.info('Role deployer created')
    def depRoles = ['deployer']
    def lJenkins = security.addUser('ci', 'ci', 'automatic', '${SHATHEL_ENV_NEXUS_CI_MAIL}', true, '${SHATHEL_ENV_NEXUS_CI_PASS}', depRoles)


    def relPrivileges = releaseRepoNames.collect{"nx-repository-view-$it-add".toString()} +
            releaseRepoNames.collect{"nx-repository-view-$it-edit".toString()}
    def relRoles = ['developer', 'deployer']
    security.addRole('releaser', 'Releaser', 'User with privileges to allow deployment release repositories', relPrivileges, relRoles)
    security.addUser('release', 'release', 'automatic', '${SHATHEL_ENV_NEXUS_RELEASE_MAIL}', true, '${SHATHEL_ENV_NEXUS_RELEASE_PASS}', ['releaser'])



/*Return a JSON response containing our new Users for confirmation*/
    return JsonOutput.toJson([johnDoe, lJenkins])
}