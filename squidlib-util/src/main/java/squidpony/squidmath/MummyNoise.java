package squidpony.squidmath;

import squidpony.annotation.Beta;

import static squidpony.squidmath.Noise.cerp;
import static squidpony.squidmath.Noise.longFloor;
import static squidpony.squidmath.WhirlingNoise.grad3d;

/**
 * A different kind of noise that has spotted and striped areas, like a tabby cat.
 * Highly experimental and expected to change; currently has significant linear artifacts, though they do wiggle.
 * Created by Tommy Ettinger on 9/2/2017.
 */
@Beta
public class MummyNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {
    public static final MummyNoise instance = new MummyNoise();
    
    public static final double[][] goldenDouble = {
            {0.6180339887498949},
            {0.7548776662466927, 0.5698402909980532},
            {0.8191725133961645, 0.6710436067037893, 0.5497004779019703},
            {0.8566748838545029, 0.733891856627126, 0.6287067210378087, 0.5385972572236101},
            {0.8812714616335696, 0.7766393890897682, 0.6844301295853426, 0.6031687406857282, 0.5315553977157913},
            {0.8986537126286993, 0.8075784952213448, 0.7257334129697598,
                    0.6521830259439717, 0.5860866975779695, 0.5266889867007359}
    };
    public static final long[][] goldenLong = {
            {0x9E3779B97F4A7C15L},
            {0xC13FA9A902A6328FL, 0x91E10DA5C79E7B1CL},
            {0xD1B54A32D192ED03L, 0xABC98388FB8FAC02L, 0x8CB92BA72F3D8DD7L},
            {0xDB4F0B9175AE2165L, 0xBBE0563303A4615FL, 0xA0F2EC75A1FE1575L, 0x89E182857D9ED688L},
            {0xE19B01AA9D42C633L, 0xC6D1D6C8ED0C9631L, 0xAF36D01EF7518DBBL, 0x9A69443F36F710E6L, 0x881403B9339BD42DL},
            {0xE60E2B722B53AEEBL, 0xCEBD76D9EDB6A8EFL, 0xB9C9AA3A51D00B65L, 0xA6F5777F6F88983FL, 0x9609C71EB7D03F7AL,
                    0x86D516E50B04AB1BL}, 
            {0xE95E1DD17D35800DL, 0xD4BC74E13F3C782EL, 0xC1EDBC5B5C68AC24L, 0xB0C8AC50F0EDEF5CL, 0xA127A31C56D1CDB5L,
                    0x92E852C80D153DB2L, 0x85EB75C3024385C3L},
            {0xEBEDEED9D803C815L, 0xD96EB1A810CAAF5EL, 0xC862B36DAF790DD5L, 0xB8ACD90C142FE10BL, 0xAA324F90DED86B69L,
                    0x9CDA5E693FEA10AEL, 0x908E3D2C82567A73L, 0x8538ECB5BD456EA2L},
            {0xEDF84ED4185625ADL, 0xDD35B2CD88449739L, 0xCDA146AB203DAD88L, 0xBF25C1FA63435002L, 0xB1AF5C04970044E1L,
                    0xA52BB0C808635D87L, 0x9989A7D8994B239DL, 0x8EB95D05457AA16AL, 0x84AC0AA2B7D7E1C4L},
            {0xEFA239AADFF080FFL, 0xE0504E7A17640707L, 0xD1F91E9D401F2CD2L, 0xC48CA286CD4C4B4CL, 0xB7FBD901347B3E44L,
                    0xAC38B6695442DF74L, 0xA13614FB593F897CL, 0x96E7A62094FC418FL, 0x8D41E4ADD988EA91L,
                    0x843A0802F95827ACL},
            {0xF104272092F57C5DL, 0xE2E8D1BC93B1BCBFL, 0xD5A0DBC4254CF233L, 0xC91FE60DC666F779L, 0xBD5A4AD0037BF1F8L,
                    0xB24512C7D7E9D953L, 0xA7D5EB01A237A78AL, 0x9E031B3B27A07ECCL, 0x94C37CD5B1796B92L, 
                    0x8C0E724FD5446177L, 0x83DBDF3EF6A3B35AL}, 
            {0xF22EECF611D9436EL, 0xE51CC09B3D83CAFFL, 0xD8BF2D5017352FF9L, 0xCD0C73D06EEC786BL, 0xC1FB5B846B56C221L,
                    0xB7832B3BCCB1A38AL, 0xAD9BA24D9CF0D513L, 0xA43CF216E1960C64L, 0x9B5FB7D32E8D899FL,
                    0x92FCF6CA403AB5A5L, 0x8B0E12CE05E62F5BL, 0x838CCB04C5269A59L},
            {0xF32E81F362A69FDCL, 0xE701532702B6F82DL, 0xDB70396E42C39DCBL, 0xD073641228368B05L, 0xC60366898AB7FC8FL,
                    0xBC193374F397645CL, 0xB2AE17DAC99253B9L, 0xA9BBB6A090C6F39DL, 0xA13C043E2FE475FBL,
                    0x992942A852DFBF6EL, 0x917DFD6F28AFDA5DL, 0x8A35060EDCF5BD75L, 0x8349706F500D7573L},
            {0xF40BA295557EB087L, 0xE8A62E740B1030B6L, 0xDDC8F72B7958B04EL, 0xD36DA0129AF999DDL, 0xC98E188E5138882CL,
                    0xC02498843215A686L, 0xB72B9CF7CB66736FL, 0xAE9DE4D05F66AF41L, 0xA6766DC536E4D933L,
                    0x9EB0716EBBC7DEE5L, 0x9747627AA435C840L, 0x9036EA018B2BCF9EL, 0x897AE4FC66EB83E8L,
                    0x830F61D86049BD32L},
            {0xF4CCD627D640C91AL, 0xEA171C21F30F9702L, 0xDFD9550E85ACC5E5L, 0xD60E418421899E89L, 0xCCB0DCDF5F586A6BL,
                    0xC3BC5AB09BC44545L, 0xBB2C24468285A234L, 0xB2FBD654234A1D55L, 0xAB273EB15C029010L,
                    0xA3AA5A3471A8B1F5L, 0x9C8152A3BD74C39CL, 0x95A87CBE60D27129L, 0x8F1C565AFE309975L,
                    0x88D9849B80100109L, 0x82DCD235027EF537L},
            {0xF57716BC263D1509L, 0xEB5D28EA462157E8L, 0xE1ADA55D371AAD50L, 0xD8642B04DD8237ECL, 0xCF7C86F34CD519B2L,
                    0xC6F2B276C5AAA9AEL, 0xBEC2D147B3AAF99AL, 0xB6E92FC9D8D36E9CL, 0xAF62415FDC02D5B1L,
                    0xA82A9ED07916A9A9L, 0xA13F04BC98DBF8F5L, 0x9A9C52259EBB171FL, 0x943F870341597D82L,
                    0x8E25C2E84A744115L, 0x884C43B5A0F27CECL, 0x82B0645B06A50EE9L},
            {0xF60E40931CDDA778L, 0xEC7F64E5DBDB0913L, 0xE34F959426E19B61L, 0xDA7B216D11305E35L, 0xD1FE7BF6F82D83FAL,
                    0xC9D63C0265C9E3CBL, 0xC1FF1A4B21B8D14FL, 0xBA75F026E47231D2L, 0xB337B641246F31A0L,
                    0xAC4183637B560C8EL, 0xA5908B4A25D90F57L, 0x9F221D8425EA54FBL, 0x98F3A45E9392C09EL,
                    0x9302A3DAAD3932B0L, 0x8D4CB8AE3C78C515L, 0x87CF974DE8CBB306L, 0x82890B01154E20B6L},
            {0xF6955E0400B9370BL, 0xED8367D64493BDFCL, 0xE4C6DA7D4C76061CL, 0xDC5C91B62A61DB7EL, 0xD44186D34B534C90L,
                    0xCC72CFA5E47E2501L, 0xC4ED9D719FBD4866L, 0xBDAF3BEA26B6853AL, 0xB6B5103A2FC2128CL,
                    0xAFFC9813B310185CL, 0xA98368C8F1D19F36L, 0xA3472E6DFC563E58L, 0x9D45AB02671DFC06L,
                    0x977CB5A2E1D02BB2L, 0x91EA39C265DDB74EL, 0x8C8C366AB5503245L, 0x8760BD83E4E84FC6L, 
                    0x8265F322AF36CE00L},
            {0xF70EDC6AB8A7E2E4L, 0xEE6DAE32C9C7C40FL, 0xE619AA5C2E8FBF99L, 0xDE101EE43CF85908L, 0xD64E71E254B519EBL,
                    0xCED220B05B09F363L, 0xC798BF1ABDC35582L, 0xC09FF697BA00A33CL, 0xB9E58585A5DA9484L,
                    0xB3673E6FFE33FC92L, 0xAD23075AFC340498L, 0xA716D9157805F047L, 0xA140BE90E084A136L,
                    0x9B9ED43F116FF0DAL, 0x962F4775D3B041B2L, 0x90F055D7D501F50CL, 0x8BE04CC2E6275AE6L,
                    0x86FD88C35074C7CFL, 0x8246750C15304E84L},
            {0xF77CB1D63A7571DBL, 0xEF41DBE827E13397L, 0xE74D154843A9A0D5L, 0xDF9C098CE341C575L, 0xD82C7821919CDE34L,
                    0xD0FC339E3959C3CCL, 0xCA092123EC35F338L, 0xC35137BF17FCC3AFL, 0xBCD27FCEFABE3A2EL,
                    0xB68B127229A241CEL, 0xB07918F7FF29533BL, 0xAA9ACC56C71B338AL, 0xA4EE74A67FC7D707L,
                    0x9F7268A00996078FL, 0x9A250D209F279448L, 0x9504D4B1719BEB04L, 0x90103F1345B068D4L,
                    0x8B45D8CDEFA9C9ADL, 0x86A43AC38D143EAAL, 0x822A09C75C802C88L}, 
            {0xF7E0785B530538D4L, 0xF002ED13129B1B36L, 0xE8654623D5A4FDADL, 0xE1057C8C52BB6F51L, 0xD9E199C336E9A782L,
                    0xD2F7B7315EB93DD4L, 0xCC45FDB04DEF7DC2L, 0xC5CAA50CC380F20FL, 0xBF83F38D4857DDEFL,
                    0xB9703D7C98999134L, 0xB38DE4B7C81D2095L, 0xADDB584003C451D7L, 0xA85713CFD25DC0C1L,
                    0xA2FF9F73B8A5A6FDL, 0x9DD38F2624E185A0L, 0x98D1826E877270B0L, 0x93F824037E9226F2L,
                    0x8F46296FFC40AC67L, 0x8ABA52BB4E330397L, 0x86536A13F0581799L, 0x8210437D13472270L},
            {0xF83B8240091653CAL, 0xF0B35A315D648BE2L, 0xE965B32CE508D7D1L, 0xE250C6C3CFEBE013L, 0xDB72DC5121B720FAL,
                    0xD4CA488E97BEA200L, 0xCE556D2CCED5DE29L, 0xC812B86E8FD1E675L, 0xC200A4C72A3ED8E2L,
                    0xBC1DB87BC58CE52FL, 0xB668854791B1725EL, 0xB0DFA802C0EBB9F0L, 0xAB81C84C35098321L,
                    0xA64D9835CB30BB8AL, 0xA141D3F331D5A3FEL, 0x9C5D418B3523501AL, 0x979EB08B6EB57305L,
                    0x9304F9BE461709CBL, 0x8E8EFEE330098749L, 0x8A3BAA691B23DA5BL, 0x8609EF2AF8E01E37L,
                    0x81F8C82E52B2202FL},
            {0xF88EE8FF789628F0L, 0xF1553336491CBF68L, 0xEA5142850AE51B39L, 0xE38186C741F35A1EL, 0xDCE47B7A35BE51FDL,
                    0xD678A76661649983L, 0xD03C9C4B680D5D39L, 0xCA2EF68E7ABB3D35L, 0xC44E5CEB1D62C9BAL,
                    0xBE998026399D81E9L, 0xB90F1AC36DD59600L, 0xB3ADF0BC88463AFAL, 0xAE74CF3B1DA834E2L,
                    0xA9628C542BDA7AC6L, 0xA47606C5B94B9652L, 0x9FAE25B66259BB0EL, 0x9B09D876C64DA00AL,
                    0x96881644C5FF0226L, 0x9227DE10869975B8L, 0x8DE836432B5CEA6EL, 0x89C82C873997130AL,
                    0x85C6D5929A71D953L, 0x81E34CF22E8F3AB1L},
            {0xF8DB97E577F72AD3L, 0xF1EA32A9D52AA512L, 0xEB2A63F7D9899909L, 0xE49AC9A46BDAC4B2L, 0xDE3A0B65FEAB39B7L,
                    0xD806DA8DFDBFA252L, 0xD1FFF1C43390BC38L, 0xCC2414C418C30C8AL, 0xC672101BFFEE3FA4L,
                    0xC0E8B8EE106A4A36L, 0xBB86ECB303374A3CL, 0xB64B90FE957169D3L, 0xB1359345A41BB63AL,
                    0xAC43E8A5E5640D21L, 0xA7758DAF33D7037FL, 0xA2C9862E604D0CECL, 0x9E3EDCF97FAA41C4L,
                    0x99D4A3BDA9D7FDB1L, 0x9589F2CE1FAB3F64L, 0x915DE8F4CDB444F4L, 0x8D4FAB44223B658BL,
                    0x895E64EA2CF4AF71L, 0x85894704FF37673EL, 0x81CF887843C83839L},
            {0xF92253C6C954B5C5L, 0xF273CB930FDEC503L, 0xEBF323BAE3351016L, 0xE59F21429203FE46L, 0xDF7691A1104F820CL,
                    0xD9784A85F6EB64D4L, 0xD3A329A111303B67L, 0xCDF6146B6E3EC0D3L, 0xC86FF7F1EB6CAABEL,
                    0xC30FC8A12DBD67AEL, 0xBDD48213008EABB2L, 0xB8BD26DD0FE44679L, 0xB3C8C060F50084BFL,
                    0xAEF65E9D8C36622FL, 0xAA4518018B211C65L, 0xA5B4093F4EA957BCL, 0xA1425521D87B064AL,
                    0x9CEF2462F3C8ABEEL, 0x98B9A58279707AEBL, 0x94A10C9EABCE2517L, 0x90A4934DA2B9292BL,
                    0x8CC37877C062E3CCL, 0x88FD003327F9CA5FL, 0x855073A02F27FBFBL, 0x81BD20C6C3B2CC6CL},
            {0xF963C86534AEA218L, 0xF2F342B97BA0C0B8L, 0xECAD4E274F138B1AL, 0xE690D14E6B40C9ACL, 0xE09CBA1281C008E1L,
                    0xDACFFD6B32C8E31CL, 0xD529973543EC46F3L, 0xCFA88A050C12366AL, 0xCA4BDEFA0CBFAB77L,
                    0xC512A593B0DD1777L, 0xBFFBF38729695FDBL, 0xBB06E49660B752F0L, 0xB6329A67FD045CFEL,
                    0xB17E3C606B66D0F5L, 0xACE8F77BEC406867L, 0xA871FE299A8EBD04L, 0xA4188827679F71F9L,
                    0x9FDBD25F04D89540L, 0x9BBB1EC3B56F7790L, 0x97B5B4310210DC66L, 0x93CADE4A48A4FA01L,
                    0x8FF9ED5B228051A6L, 0x8C4236389B77FA32L, 0x88A312233474905FL, 0x851BDEA9AC42A769L,
                    0x81ABFD8C89825095L},
            {0xF9A06624E78891FDL, 0xF3696B33A14A8488L, 0xED5A0C474FD5E98DL, 0xE7714CED2171F9F8L, 0xE1AE36FB3BA4C4A2L,
                    0xDC0FDA68AC8CF831L, 0xD6954D265B8914FBL, 0xD13DAAF8F2D0DF40L, 0xCC081553BBCE6CCFL,
                    0xC6F3B334682BA958L, 0xC1FFB0FFC1AFB181L, 0xBD2B405F3B2CF3C7L, 0xB875981F5CE5A61EL,
                    0xB3DDF40F06EFD576L, 0xAF6394DF8445271BL, 0xAB05C005694C58A6L, 0xA6C3BF9A38CBA782L,
                    0xA29CE23ECA539283L, 0x9E907AFE6D50E36AL, 0x9A9DE132C413A13FL, 0x96C4706852367BA0L,
                    0x93038843B9EF6F7DL, 0x8F5A8C67A3EEDC5EL, 0x8BC8E45B4D8E0211L, 0x884DFB71B928EF01L,
                    0x84E940B17C9A4ADDL, 0x819A26BD29E924F1L},
            {0xF9D8D544F27EF365L, 0xF3D78688B5BFF9CFL, 0xEDFB2AD87B4B3897L, 0xE842DEDAC95F23FCL, 0xE2ADC4AD07A91259L,
                    0xDD3B03C1DFF6CBCDL, 0xD7E9C8C06DC8D23FL, 0xD2B9456437CC7685L, 0xCDA8B05DEE6367FAL,
                    0xC8B74534EA7C4797L, 0xC3E44429681CEC1BL, 0xBF2EF217781B75B6L, 0xBA96985AA49F118FL,
                    0xB61A84B2441C64E3L, 0xB1BA0926769C07BDL, 0xAD747BEDC9334546L, 0xA94937537BB0869BL,
                    0xA537999E6495613FL, 0xA13F04F86F903F0EL, 0x9D5EDF56B2BEFEE1L, 0x99969262171AC642L,
                    0x95E58B6090838939L, 0x924B3B1EE1F79098L, 0x8EC715DAEA9884BFL, 0x8B58932E78343D8AL,
                    0x87FF2DFA9C1BD382L, 0x84BA64537F272D05L, 0x8189B76CB1D68797L}, 
            {0xFA0DCF5005A2C20BL, 0xF43EF9A70104F7A6L, 0xEE92ACCB096678F8L, 0xE9081B643A4C436AL, 0xE39E7CDFAAD55A78L,
                    0xDE550D5311B2B77AL, 0xD92B0D6111BFC621L, 0xD41FC21E2B50C306L, 0xCF3274F64E639BAEL,
                    0xCA62739309F6AEB6L, 0xC5AF0FC254DF031DL, 0xC1179F5DEC8D41A6L, 0xBC9B7C334645EE55L,
                    0xB83A03EC0F6614ECL, 0xB3F297F73961D544L, 0xAFC49D728E3DFDC6L, 0xABAF7D14CA592E83L,
                    0xA7B2A318386BD8B2L, 0xA3CD7F25CCB8D7A0L, 0x9FFF8440BC7A5BFEL, 0x9C4828B28EA8733CL,
                    0x98A6E5F7A3479D28L, 0x951B38AC2E7EA456L, 0x91A4A079A4C34CACL, 0x8E42A004957D68BDL,
                    0x8AF4BCDAF18F8514L, 0x87BA7F62BB4399E1L, 0x849372C91D291A5EL, 0x817F24F1E580474DL},
            {0xFA3F3C2E14D0B6F9L, 0xF49F9128AD7B8627L, 0xEF204087E102D835L, 0xE9C0902B2D8A1227L, 0xE47FCA20DA7EAFEFL,
                    0xDF5D3C8DE85FC90AL, 0xDA5839968AF541FFL, 0xD57017472ADA3EABL, 0xD0A42F7DEB504F2FL,
                    0xCBF3DFD4B162501BL, 0xC75E898BA96F10A0L, 0xC2E391744844896EL, 0xBE825FDCC504C678L,
                    0xBA3A607C091D9F6CL, 0xB60B025E13ABF879L, 0xB1F3B7D0CDB288BBL, 0xADF3F6514C9B0FC4L,
                    0xAA0B3679808779EAL, 0xA638F3EE4C06B542L, 0xA27CAD4E02CEE96FL, 0x9ED5E41F4D2C6489L,
                    0x9B441CC06DE1E0F4L, 0x97C6DE56E843CD29L, 0x945DB2BF8464F886L, 0x9108267EAF377921L,
                    0x8DC5C8B13490C810L, 0x8A962AFD510BF4E0L, 0x8778E18419D07004L, 0x846D82D3384F4823L,
                    0x8173A7D6F813D0F5L},
            {0xFA69614E16899957L, 0xF4F1FD6D7B0F8145L, 0xEF9925D84466C496L, 0xEA5E2FD7D598D5ACL, 0xE540746F939348F2L,
                    0xE03F504811D1E061L, 0xDB5A239AB36A0205L, 0xD690521DBDED3942L, 0xD1E142F0DBA6950EL,
                    0xCD4C608A0AC49885L, 0xC8D118A2F70F08DDL, 0xC46EDC26BBD52D64L, 0xC0251F200BCE15A6L,
                    0xBBF358A7BCB1308AL, 0xB7D902D3B459EF1CL, 0xB3D59AA6355557BCL, 0xAFE89FFD88C454DAL,
                    0xAC11958403892E12L, 0xA85000A064C1FD25L, 0xA4A369668B9D1383L, 0xA10B5A88829F29C1L,
                    0x9D876147DE7DDA76L, 0x9A170D676EBB5655L, 0x96B9F11D3E3A7168L, 0x936FA104E20C22B8L,
                    0x9037B41214C150EAL, 0x8D11C3839C95515AL, 0x89FD6AD67ACED4EDL, 0x86FA47B962BE1F1BL,
                    0x8407FA0076C85236L, 0x8126239949F8586FL}, 
            {0xFA905D0C27652E4AL, 0xF53E47248C6FC570L, 0xF0091DA4FA352D2AL, 0xEAF043527E47412AL, 0xE5F31E48DD9832FCL,
                    0xE11117E86E2C176EL, 0xDC499CC4537425AFL, 0xD79C1C911B3B5169L, 0xD3080A13B9174C7EL,
                    0xCE8CDB10DE5C22D6L, 0xCA2A083CAC9B8BB6L, 0xC5DF0D2AC0C3B9DFL, 0xC1AB683E94FBE76EL,
                    0xBD8E9A9C37671548L, 0xB988281953FF794FL, 0xB597972E8FC6E33FL, 0xB1BC70E93391F7E2L,
                    0xADF640DD24BD8588L, 0xAA4495172A266536L, 0xA6A6FE0F7BC55A65L, 0xA31D0E9C9B5930B3L,
                    0x9FA65BE67491F4B0L, 0x9C427D59C33893B4L, 0x98F10C9BBDD66FC7L, 0x95B1A57E036888B9L,
                    0x9283E5F2CAB1C3D4L, 0x8F676E0151C798CFL, 0x8C5BDFBA8C7BFDC4L, 0x8960DF2E104ED60EL,
                    0x8676125F3C986987L, 0x839B213A9DA583F5L, 0x80CFB58B8984D00CL}
    };
    
    public long seedX, seedY, seedZ, seedW, seedU, seedV;

    public MummyNoise() {
        this(0x1337BEEF);
    }

    public MummyNoise(final int seed) {
        seedX = 0x9E3779B97F4A7C15L * LinnormRNG.determine(seed + 0xC6BC279692B5CC83L);
        seedY = 0x9E3779B97F4A7C15L * LinnormRNG.determine(seedX ^ 0xC7BC279692B5CB83L);
        seedZ = 0x9E3779B97F4A7C15L * LinnormRNG.determine(seedY ^ 0xC8BC279692B5CA83L);
        seedW = 0x9E3779B97F4A7C15L * LinnormRNG.determine(seedZ ^ 0xC9BC279692B5C983L);
        seedU = 0x9E3779B97F4A7C15L * LinnormRNG.determine(seedW ^ 0xCABC279692B5C883L);
        seedV = 0x9E3779B97F4A7C15L * LinnormRNG.determine(seedU ^ 0xCBBC279692B5C783L);
    }

    /**
     * The same as {@link LinnormRNG#determine(long)}, except this assumes state has already been multiplied by
     * 0x632BE59BD9B4E019L.
     * @param state a long that should change in increments of 0x632BE59BD9B4E019L
     * @return a pseudo-random permutation of state
     */
    public static long determine(long state)
    {
        return (state = ((state = ((state ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25;
    }

    //    public static double gauss(final long state) {
//        final long s1 = state + 0x9E3779B97F4A7C15L,
//                s2 = s1 + 0x9E3779B97F4A7C15L,
//                y = (s1 ^ s1 >>> 26) * 0x2545F4914F6CDD1DL,
//                z = (s2 ^ s2 >>> 26) * 0x2545F4914F6CDD1DL;
//        return ((((y ^ y >>> 28) & 0x7FFFFFL) + ((y ^ y >>> 28) >>> 41))
//                + (((z ^ z >>> 28) & 0x7FFFFFL) + ((z ^ z >>> 28) >>> 41))) * 0x1p-24 - 1.0;
//    }
//    public static double signedDouble(long state) {
//        return (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) >> 12) * 0x1p-52;
//    }
    @Override
    public double getNoise(final double x, final double y) {
        return getNoiseWithSeed(x, y, seedX);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, final long seed) {
        x *= 2.0;
        y *= 2.0;
        final long
                x0 = longFloor(x),
                y0 = longFloor(y);
        return Noise.emphasizeSigned(
                cerp(cerp(gradCoord2D(seed, x0, y0, x - x0, y - y0), gradCoord2D(seed, x0+1, y0, x - x0 - 1, y - y0), x - x0),
                        cerp(gradCoord2D(seed, x0, y0+1, x - x0, y - y0-1), gradCoord2D(seed, x0+1, y0+1, x - x0 - 1, y - y0 - 1), x - x0),
                        y - y0));// * 0.875);// * 1.4142;
    }

    @Override
    public double getNoise(final double x, final double y, final double z) {
        return getNoiseWithSeed(x, y, z, seedX);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, final long seed) {
        x *= 2.0;
        y *= 2.0;
        z *= 2.0;
        final long
                x0 = longFloor(x),
                y0 = longFloor(y),
                z0 = longFloor(z);
        return Noise.emphasizeSigned(
                cerp(cerp(cerp(gradCoord3D(seed, x0, y0, z0, x - x0, y - y0, z - z0), gradCoord3D(seed, x0+1, y0, z0, x - x0 - 1, y - y0, z - z0), x - x0),
                        cerp(gradCoord3D(seed, x0, y0+1, z0, x - x0, y - y0-1, z - z0), gradCoord3D(seed, x0+1, y0+1, z0, x - x0 - 1, y - y0 - 1, z - z0), x - x0),
                        y - y0),
                        cerp(cerp(gradCoord3D(seed, x0, y0, z0+1, x - x0, y - y0, z - z0-1), gradCoord3D(seed, x0+1, y0, z0+1, x - x0 - 1, y - y0, z - z0-1), x - x0),
                                cerp(gradCoord3D(seed, x0, y0+1, z0+1, x - x0, y - y0-1, z - z0-1), gradCoord3D(seed, x0+1, y0+1, z0+1, x - x0 - 1, y - y0 - 1, z - z0-1), x - x0),
                                y - y0), z - z0) * 0.666);//1.0625;
    }

//    @Override
//    public double getNoise(double x, double y) {
//        return getNoiseWithSeeds(x, y, seedX, seedY);
//    }
//
//    @Override
//    public double getNoiseWithSeed(double x, double y, final long seed) {
//        //0.7548776662466927, 0.5698402909980532
//        //0xC13FA9A902A6328FL, 0x91E10DA5C79E7B1CL
//        final long
//                x0 = longFloor(x), 
//                y0 = longFloor(y);
//        final double res =
////        return
//                Noise.emphasizeSigned
//                        (cerp(cerp(gradCoord2D(seed, x0, y0, x - x0, y - y0), gradCoord2D(seed, x0+1, y0, x - x0 - 1, y - y0), x - x0),
//                        cerp(gradCoord2D(seed, x0, y0+1, x - x0, y - y0-1), gradCoord2D(seed, x0+1, y0+1, x - x0 - 1, y - y0 - 1), x - x0),
//                        y - y0));// * 1.4142);//* 0.875;// * 1.4142;
//        if(res < -1.0 || res > 1.0) System.out.println(res);
//        return res;
//
////        double r = NumberTools.setExponent(NumberTools.swayRandomized(seed + 0xC13FA9A902A6328FL, y * 0.5698402909980532) + NumberTools.swayRandomized(seed, x * 0.7548776662466927) + 6.0, 0x400) - 3.0;
////        return r;
////        x += 75.48776662466927;
////        y += 56.98402909980532;
////        final double x1 = x * -0.3623748900804805 - y * 0.9320324238132275, y1 = x * 0.9320324238132275 + y * -0.3623748900804805;
////        r *= NumberTools.swayRandomized(seed + 0xC13FA9A902A6328FL, y1 * (NumberTools.swayRandomized(seed, x1 * 0.7548776662466927) * 1.5698402909980532));
////        x = x1 - 75.48776662466927;
////        y = y1 - 56.98402909980532;
////        final double x2 = x * -0.7373688780783193 - y * -0.6754902942615243, y2 = x * -0.6754902942615243 + y * -0.7373688780783193;
////        return r * NumberTools.swayRandomized(seed + 0xC13FA9A902A6328FL, y2 * (NumberTools.swayRandomized(seed, x2 * 0.7548776662466927) * 1.5698402909980532));
//    }

    private long hash2D(long seed, long x, long y)
    {
        return ((x = ((x = x * 0xC6BC279692B5CC85L + seed) ^ x >>> 26)
                * ((y *= 0x9E3779B97F4A7C15L) ^ (y + 0x9E3779B97F4A7C15L))) ^ x >>> 28);
    }
    private long hash3D(long seed, long x, long y, long z)
    {
        return (z = ((x = ((x = x * 0xC6BC279692B5CC85L + seed) ^ x >>> 26)
                * ((y *= 0x9E3779B97F4A7C15L) ^ (y + 0x9E3779B97F4A7C15L))) ^ x >>> 28) * ((z *= 0x9E3779B97F4A7C15L) ^ z + 0x9E3779B97F4A7C15L)) ^ z >>> 28;
    }
    private double gradCoord2D(long seed, long x, long y, double xDiff, double yDiff) {
        seed = hash2D(seed, x, y);
        x = (seed >> 11);
        y = (seed * 0xC13FA9A902A6328FL >> 11);
        return
                x * 0x1p-52 * (xDiff + (y * 0x6p-56)) +
                        y * 0x1p-52 * (yDiff + (x * 0x6p-56));
    }
    private double gradCoord3D(long seed, long x, long y, long z, double xDiff, double yDiff, double zDiff) {
        seed = hash3D(seed, x, y, z);
        x = (seed >> 11);
        y = (seed * 0xC13FA9A902A6328FL >> 11);
        z = (seed * 0x91E10DA5C79E7B1CL >> 11);
        return
                x * 0x1p-52 * (xDiff + (y * 0x6p-56)) + 
                        y * 0x1p-52 * (yDiff + (z * 0x6p-56)) +
                z * 0x1p-52 * (zDiff + (x * 0x6p-56));
    }
//        double dist = (hash2D(seed, x, y) >> 11) * 0x1.921fb54442d18p-50;

    
//    protected static double gradCoord2D(long seed, int x, int y,
//                                        double xd, double yd) {
//        final int hash = ((int)(((seed ^= 0xB4C4D * x ^ 0xEE2C3 * y) ^ seed >>> 13) * (seed)));
//        //final int hash = (int)((((seed = (((seed * (0x632BE59BD9B4E019L + (x << 23))) ^ 0x9E3779B97F4A7C15L) * (0xC6BC279692B5CC83L + (y << 23)))) ^ seed >>> 27 ^ x + y) * 0xAEF17502108EF2D9L) >>> 56);
//        final double[] grad = phiGrad2[hash >>> 24], jitter = phiGrad2[hash >>> 16 & 0xFF];
//        return (xd + jitter[0] * 0.5) * grad[0] + (yd + jitter[1] * 0.5) * grad[1];
//    }
    protected static double gradCoord3D(long seed, int x, int y, int z, double xd, double yd, double zd) {
        final int hash = (int)(((seed ^= 0xB4C4D * x ^ 0xEE2C1 * y ^ 0xA7E07 * z) ^ seed >>> 13) * (seed)),
                idx = (hash >>> 27) * 3, jitter = (hash >>> 22 & 0x1F) * 3;
        return ((xd+grad3d[jitter]*0.5) * grad3d[idx]
                + (yd+grad3d[jitter+1]*0.5) * grad3d[idx + 1]
                + (zd+grad3d[jitter+2]*0.5) * grad3d[idx + 2]);
    }

    public static double getNoiseWithSeeds(final double x, final double y,
                                    final long seedX, final long seedY) {
        final long
                xf = longFloor(x),
                yf = longFloor(y),
                bx0 = xf * seedX,
                by0 = yf * seedY,
                bx1 = bx0+seedX,
                by1 = by0+seedY;
        return
                NumberTools.sway(
                        cerp(cerp(determine(bx0 + by0) * 0x1.25p-62, determine(bx1 + by0) * 0x1.25p-62,
                                x - xf),
                                cerp(determine(bx0 + by1) * 0x1.25p-62, determine(bx1 + by1) * 0x1.25p-62, x - xf),
                                y - yf));
    }

//    @Override
//    public double getNoise(double x, double y, double z) {
//        return getNoiseWithSeeds(x, y, z, seedX, seedY, seedZ);
//    }
//
//    @Override
//    public double getNoiseWithSeed(final double x, final double y, final double z, final long seed) {
//        final long
//                rs = LinnormRNG.determine(seed ^ ~seed << 32),
//                rx = 0x9E3779B97F4A7C15L * (rs >>> 23 ^ rs << 23) * (rs | 1L),
//                ry = 0x9E3779B97F4A7C15L * (rx >>> 23 ^ rx << 23) * (rx | 1L),
//                rz = 0x9E3779B97F4A7C15L * (ry >>> 23 ^ ry << 23) * (ry | 1L);
//        return getNoiseWithSeeds(x, y, z, rx, ry, rz);
//    }

    public static double getNoiseWithSeeds(final double x, final double y, final double z,
                                    final long seedX, final long seedY, final long seedZ) {
        final long
                xf = longFloor(x),
                yf = longFloor(y),
                zf = longFloor(z),
                bx0 = xf * seedX,
                by0 = yf * seedY,
                bz0 = zf * seedZ,
                bx1 = bx0+seedX,
                by1 = by0+seedY,
                bz1 = bz0+seedZ;
        return NumberTools.sway(
                cerp(
                        cerp(
                                cerp(determine(bx0 + by0 + bz0) * 0x1.25p-62, determine(bx1 + by0 + bz0) * 0x1.25p-62, x - xf),
                                cerp(determine(bx0 + by1 + bz0) * 0x1.25p-62, determine(bx1 + by1 + bz0) * 0x1.25p-62, x - xf),
                                y - yf),
                        cerp(
                                cerp(determine(bx0 + by0 + bz1) * 0x1.25p-62, determine(bx1 + by0 + bz1) * 0x1.25p-62, x - xf),
                                cerp(determine(bx0 + by1 + bz1) * 0x1.25p-62, determine(bx1 + by1 + bz1) * 0x1.25p-62, x - xf),
                                y - yf),
                        z - zf));
    }

    @Override
    public double getNoise(final double x, final double y, final double z, final double w) {
        return getNoiseWithSeeds(x, y, z, w, seedX, seedY, seedZ, seedW);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final long seed) {
        final long
                rs = LinnormRNG.determine(seed ^ ~seed << 32),
                rx = 0x9E3779B97F4A7C15L * (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = 0x9E3779B97F4A7C15L * (rx >>> 23 ^ rx << 23) * (rx | 1L),
                rz = 0x9E3779B97F4A7C15L * (ry >>> 23 ^ ry << 23) * (ry | 1L),
                rw = 0x9E3779B97F4A7C15L * (rz >>> 23 ^ rz << 23) * (rz | 1L);
        return getNoiseWithSeeds(x, y, z, w, rx, ry, rz, rw);
    }

    public static double getNoiseWithSeeds(final double x, final double y, final double z, final double w,
                                    final long seedX, final long seedY, final long seedZ, final long seedW) {
        final long
                xf = longFloor(x),
                yf = longFloor(y),
                zf = longFloor(z),
                wf = longFloor(w),
                bx0 = xf * seedX,
                by0 = yf * seedY,
                bz0 = zf * seedZ,
                bw0 = wf * seedW,
                bx1 = bx0+seedX,
                by1 = by0+seedY,
                bz1 = bz0+seedZ,
                bw1 = bw0+seedW;
        return NumberTools.sway(
                cerp(
                        cerp(
                                cerp(
                                        cerp(determine(bx0 + by0 + bz0 + bw0) * 0x1.25p-62, determine(bx1 + by0 + bz0 + bw0) * 0x1.25p-62, x - xf),
                                        cerp(determine(bx0 + by1 + bz0 + bw0) * 0x1.25p-62, determine(bx1 + by1 + bz0 + bw0) * 0x1.25p-62, x - xf),
                                        y - yf),
                                cerp(
                                        cerp(determine(bx0 + by0 + bz1 + bw0) * 0x1.25p-62, determine(bx1 + by0 + bz1 + bw0) * 0x1.25p-62, x - xf),
                                        cerp(determine(bx0 + by1 + bz1 + bw0) * 0x1.25p-62, determine(bx1 + by1 + bz1 + bw0) * 0x1.25p-62, x - xf),
                                        y - yf),
                                z - zf),
                        cerp(
                                cerp(
                                        cerp(determine(bx0 + by0 + bz0 + bw1) * 0x1.25p-62, determine(bx1 + by0 + bz0 + bw1) * 0x1.25p-62, x - xf),
                                        cerp(determine(bx0 + by1 + bz0 + bw1) * 0x1.25p-62, determine(bx1 + by1 + bz0 + bw1) * 0x1.25p-62, x - xf),
                                        y - yf),
                                cerp(
                                        cerp(determine(bx0 + by0 + bz1 + bw1) * 0x1.25p-62, determine(bx1 + by0 + bz1 + bw1) * 0x1.25p-62, x - xf),
                                        cerp(determine(bx0 + by1 + bz1 + bw1) * 0x1.25p-62, determine(bx1 + by1 + bz1 + bw1) * 0x1.25p-62, x - xf),
                                        y - yf),
                                z - zf),
                        w - wf));
    }

    @Override
    public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
        return getNoiseWithSeeds(x, y, z, w, u, v,
                seedX, seedY, seedZ, seedW, seedU, seedV);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, long seed) {
        final long
                rs = LinnormRNG.determine(seed ^ ~seed << 32),
                rx = 0x9E3779B97F4A7C15L * (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = 0x9E3779B97F4A7C15L * (rx >>> 23 ^ rx << 23) * (rx | 1L),
                rz = 0x9E3779B97F4A7C15L * (ry >>> 23 ^ ry << 23) * (ry | 1L),
                rw = 0x9E3779B97F4A7C15L * (rz >>> 23 ^ rz << 23) * (rz | 1L),
                ru = 0x9E3779B97F4A7C15L * (rw >>> 23 ^ rw << 23) * (rw | 1L),
                rv = 0x9E3779B97F4A7C15L * (ru >>> 23 ^ ru << 23) * (ru | 1L);
        return getNoiseWithSeeds(x, y, z, w, u, v, rx, ry, rz, rw, ru, rv);
    }

    public static double getNoiseWithSeeds(final double x, final double y, final double z,
                                    final double w, final double u, final double v,
                                    final long seedX, final long seedY, final long seedZ,
                                    final long seedW, final long seedU, final long seedV) {
        final long
                xf = longFloor(x),
                yf = longFloor(y),
                zf = longFloor(z),
                wf = longFloor(w),
                uf = longFloor(u),
                vf = longFloor(v),
                bx0 = xf * seedX,
                by0 = yf * seedY,
                bz0 = zf * seedZ,
                bw0 = wf * seedW,
                bu0 = uf * seedU,
                bv0 = vf * seedV,
                bx1 = bx0+seedX,
                by1 = by0+seedY,
                bz1 = bz0+seedZ,
                bw1 = bw0+seedW,
                bu1 = bu0+seedU,
                bv1 = bv0+seedV;
        return NumberTools.sway(
                cerp(
                        cerp(
                                cerp(
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw0 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw0 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw0 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw0 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw0 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw0 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw0 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw0 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw1 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw1 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw1 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw1 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw1 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw1 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw1 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw1 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        w - wf),
                                cerp(
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw0 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw0 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw0 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw0 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw0 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw0 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw0 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw0 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw1 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw1 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw1 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw1 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw1 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw1 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw1 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw1 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        w - wf),
                                u - uf),

                        cerp(
                                cerp(
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw0 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw0 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw0 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw0 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw0 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw0 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw0 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw0 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw1 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw1 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw1 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw1 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw1 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw1 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw1 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw1 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        w - wf),
                                cerp(
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw0 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw0 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw0 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw0 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw0 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw0 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw0 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw0 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw1 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw1 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw1 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw1 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw1 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw1 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw1 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw1 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        w - wf),
                                u - uf),
                        v - vf));
    }
    private transient long[] scratch3;
    private transient double[] scratch;
    private transient int lastLen = -1;
    private transient double lastEffect = 0x1.12bp-61;
    public final double arbitraryNoise(long seed, double... coordinates) {
        final int len = coordinates.length, upper = 1 << len;
        final double effect;
        if(len != lastLen)
        {
            lastLen = len;
            lastEffect = effect = 0x1.81p-62 * Math.pow(1.1875, len);
        }
        else
            effect = lastEffect;
        if(scratch3 == null || scratch3.length < len * 3)
            scratch3 = new long[len * 3];
        if(scratch == null || scratch.length < upper)
            scratch = new double[upper];
        for (int i = 0; i < len; i++) {
            seed = LinnormRNG.determine(seed + 0xC6BC279692B5CC83L ^ ~seed << 32);
            scratch3[i * 3 + 1] = (scratch3[i * 3] = (scratch3[i * 3 + 2] = longFloor(coordinates[i])) * seed) + seed;
        }
        long working;
        for (int i = 0; i < upper; i++) {
            working = 0L;
            for (int j = 0; j < len; j++) {
                working += scratch3[j * 3 + (i >> j & 1)];
            }
            scratch[i] = determine(working) * effect;
        }
        for (int i = 0; i < len; ++i) {
            for (int j = 0, t = upper >> i; j < t; j += 2) {
                scratch[j >> 1] = cerp(scratch[j], scratch[j + 1], coordinates[i] - scratch3[i * 3 + 2]);
            }
        }
        return NumberTools.sway(scratch[0]);
    }

}
