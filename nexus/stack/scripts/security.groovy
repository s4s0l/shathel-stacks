import org.sonatype.nexus.security.user.UserNotFoundException
def usersExist = false
try {if (security.getSecuritySystem().getUser('dev') && security.getSecuritySystem().getUser('ci')) usersExist=true} catch (UserNotFoundException ex){log.info('Will create users')}
import groovy.json.JsonOutput
security.setAnonymousAccess(false)
log.info('Anonymous access disabled')
if (!usersExist){/*To create new admin user you will have to assign nx-admin role*/
/*Create a new role that allows a user same access as anonymous and adds healtchcheck access*/
    def devPrivileges = ['nx-healthcheck-read', 'nx-healthcheck-summary-read']
    def anoRole = ['nx-anonymous']
/* add roles that uses the built in nx-anonymous role as a basis and adds more privileges*/
    security.addRole('developer', 'Developer', 'User with privileges to allow read access to repo content and healtcheck', devPrivileges, anoRole)
    log.info('Role developer created')
/* use the new role to create a user*/
    def devRoles = ['developer']
    def johnDoe = security.addUser('dev', 'dev', 'ops', '${SHATHEL_ENV_NEXUS_DEV_MAIL"}', true, '${SHATHEL_ENV_NEXUS_DEV_PASS}', devRoles)
    log.info('User developer created')
/*Create new role that allows deployment and create a user to be used on a CI server*/
/* privileges with pattern * to allow any format, browse and read are already part of nx-anonymous*/
    def depPrivileges = ['nx-repository-view-*-*-add', 'nx-repository-view-*-*-edit']
    def roles = ['developer']
/* add roles that uses the developer role as a basis and adds more privileges*/
    security.addRole('deployer', 'Deployer', 'User with privileges to allow deployment all repositories', depPrivileges, roles)
    log.info('Role deployer created')
    def depRoles = ['deployer']
    def lJenkins = security.addUser('ci', 'ci', 'automatic', '${SHATHEL_ENV_NEXUS_CI_MAIL}', true, '${SHATHEL_ENV_NEXUS_CI_PASS}', depRoles)
    log.info('User jenkins created')
    log.info('Script security completed successfully')
/*Return a JSON response containing our new Users for confirmation*/
    return JsonOutput.toJson([johnDoe, lJenkins])}