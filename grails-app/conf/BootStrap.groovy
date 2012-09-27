import org.codehaus.groovy.grails.commons.GrailsApplication;

import com.nnapz.hbaseexplorer.domain.security.Requestmap
import com.nnapz.hbaseexplorer.domain.security.Role
import com.nnapz.hbaseexplorer.domain.security.User
import com.nnapz.hbaseexplorer.domain.security.UserRole;
import com.nnapz.hbaseexplorer.services.ConfigHolderService;
import com.nnapz.hbaseexplorer.services.HbaseService


class BootStrap {

	def springSecurityService

	GrailsApplication grailsApplication
	
	public static final String MRJAR_SOURCE_PATH = '/WEB-INF/lib/hbe.jar'

	def pathToMRJar

	def init = { servletContext ->

		//security context setup
		
		if (grailsApplication.getFlatConfig().get("grails.plugins.springsecurity.active") != false){
			initializeSecurity()
			Requestmap.executeUpdate('delete from Requestmap') //clear request map
			
			if (grailsApplication.getFlatConfig().get("hbaseexplorer.security.active") != false){
				new Requestmap(url: '/hbaseSource/threads/**', configAttribute: 'IS_AUTHENTICATED_ANONYMOUSLY').save()
				new Requestmap(url: '/hbaseSource/**', configAttribute: 'IS_AUTHENTICATED_REMEMBERED').save()
				new Requestmap(url: '/hbasePattern/**', configAttribute: 'IS_AUTHENTICATED_REMEMBERED').save()
				new Requestmap(url: '/hbaseTableStats/**', configAttribute: 'IS_AUTHENTICATED_REMEMBERED').save()
				new Requestmap(url: '/', configAttribute: 'IS_AUTHENTICATED_REMEMBERED').save()
				new Requestmap(url: '/user/**', configAttribute: 'ROLE_ROOT').save()
			} else {
				new Requestmap(url: '/**', configAttribute: 'IS_AUTHENTICATED_ANONYMOUSLY').save()
				new Requestmap(url: '/user/**', configAttribute: 'ROLE_ROOT').save()
			}
			springSecurityService.clearCachedRequestmaps();
		}
		
		
		
		// to be fully complient with the j2ee Specs, we need to get our own file copy of the jar. Access by
		// servletContext.getResourcePaths('/WEB-INF/lib/') or servletContext.getResource('/WEB-INF/lib/hbe.jar') may
		// not work.
		try {
			URL sourceURL = servletContext.getResource(MRJAR_SOURCE_PATH)
			if (sourceURL == null){
				throw new FileNotFoundException("Url to source MRJar was null");
			}
			
			FileInputStream sourceStream = new FileInputStream(sourceURL.getPath());

			File tmpDir = (File) servletContext.getAttribute('javax.servlet.context.tempdir');
			File destinationFile = new File(tmpDir,'hbe.jar')

			//remove file if already exists
			if (destinationFile.exists())
				destinationFile.delete()

			destinationFile.withOutputStream{ out -> sourceStream.eachByte{ out.write it }}

			//evaluate relative location of file
			File tmpFile = new File('')
			pathToMRJar = tmpFile.toURI().relativize(destinationFile.toURI()).getPath()

			println 'Use Production Locaton for MR Jar: ' + pathToMRJar
		} catch (FileNotFoundException fnfex){
			pathToMRJar = 'lib/hbe.jar';
			println 'Use Development Location for MR Jar: ' + pathToMRJar
		}
		
		servletContext.setProperty(ConfigHolderService.PATH_TO_MR_JAR, pathToMRJar);
	}

	
	private def initializeSecurity = {
		def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(failOnError: true)
		def rootRole = Role.findByAuthority('ROLE_ROOT') ?: new Role(authority: 'ROLE_ROOT').save(failOnError: true)
		def hbaseAdminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(failOnError:true)
		def adminUser = User.findByUsername('admin') ?: new User(
			username: 'admin',
			password: 'admin',
			enabled: true).save(failOnError: true)

		if (!adminUser.authorities.contains(rootRole)) {
				def role = UserRole.create adminUser, rootRole
				role.save(failOnError: true);
		}
	}
	

	def destroy = {
		//TODO add cleanup actions there
	}

}
