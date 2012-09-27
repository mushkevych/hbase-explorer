import com.nnapz.hbaseexplorer.domain.HbaseFamilyStats
import com.nnapz.hbaseexplorer.domain.HbaseSource
import com.nnapz.hbaseexplorer.domain.HbaseTableStats
import grails.test.GrailsUnitTestCase

import grails.test.*


class HbaseTableStatsTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testGormRelations() {
        HbaseSource hs = new HbaseSource(name: 'test', quorumPort: 1234, quorumServers: 'xxx')
        assert storeIt(hs)
        HbaseTableStats ts = new HbaseTableStats(hbaseSource: hs, tableName: 'mytable')
        assert storeIt(ts)
        HbaseFamilyStats fs = new HbaseFamilyStats(hbaseTableStats: ts, familyName: 'f1')
        assert storeIt(fs)
        ts.addToHbaseFamilyStats(fs)

        assert ts.hbaseFamilyStats.size() == 1
    }

  private def storeIt(def hs) {
    boolean success = hs.save()
    if (!success) { hs.errors.each { println it  }}
    return success
  }
}