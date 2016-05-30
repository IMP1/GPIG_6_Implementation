package movement;

import java.time.LocalDateTime;
import java.util.ArrayList;

import broadcast.Broadcast;
import datastore.Datastore;
import datastore.Drone;
import drones.sensors.SensorInterface;
import gpig.all.schema.Coord;
import network.Message;
import network.ScanData;
import network.StatusData;
import network.StatusData.DroneState;

public class MovementThread implements Runnable {

	public double[] positions  = {
			53.95467930826194, -1.0796186327934265,
			53.95468404309817, -1.0796481370925903,
			53.95469351276901, -1.079680323600769,
			53.95470140415974, -1.0796990990638733,
			53.95470929554897, -1.0797151923179626,
			53.954714030381794, -1.0797339677810667,
			53.95472823487701, -1.079755425453186,
			53.95472981315397, -1.079782247543335,
			53.95474401764384, -1.079806387424469,
			53.954751909025006, -1.079830527305603,
			53.95476295695611, -1.0798439383506775,
			53.954780317984806, -1.0798707604408264,
			53.95479610073192, -1.0798922181129456,
			53.95480872692529, -1.07990562915802,
			53.95481977484136, -1.0799217224121094,
			53.95483555757351, -1.079929769039154,
			53.95484660548248, -1.0799458622932434,
			53.954863966476346, -1.0799646377563477,
			53.95488132746298, -1.079983413219452,
			53.95489553190125, -1.0800021886825562,
			53.9549160494146, -1.0800182819366455,
			53.95493498864875, -1.0800397396087646,
			53.95494919306871, -1.0800611972808838,
			53.954961819215725, -1.080082654953003,
			53.95498075842909, -1.0801094770431519,
			53.955004432433675, -1.0801255702972412,
			53.95502494989346, -1.0801497101783752,
			53.955042310813084, -1.0801604390144346,
			53.95506282825422, -1.080181896686554,
			53.95508650221222, -1.0802087187767029,
			53.95510544136894, -1.080230176448822,
			53.955129115302746, -1.0802489519119263,
			53.95515278922311, -1.0802677273750305,
			53.95517172834974, -1.0802838206291197,
			53.95519066746776, -1.080305278301239,
			53.95520487180062, -1.0803240537643433,
			53.95523012393598, -1.0803481936454773,
			53.95525064128471, -1.0803669691085815,
			53.95526642385378, -1.080380380153656,
			53.95528694118464, -1.08040452003479,
			53.95531219327029, -1.0804179310798645,
			53.95533586708672, -1.0804474353790283,
			53.95535796263659, -1.0804662108421326,
			53.95538637118348, -1.0804876685142517,
			53.955413201460004, -1.0805091261863708,
			53.95544003171928, -1.08052521944046,
			53.95545739246607, -1.0805439949035645,
			53.95547948795155, -1.0805627703666687,
			53.95549684868191, -1.080578863620758,
			53.955520522393485, -1.080605685710907,
			53.955545774337686, -1.0806137323379517,
			53.95556629153109, -1.0806244611740112,
			53.955585230469886, -1.0806459188461304,
			53.95560890413128, -1.0806700587272644,
			53.95563099953646, -1.0806888341903687,
			53.955656251413764, -1.0807102918624876,
			53.95567992503483, -1.080731749534607,
			53.95570044216221, -1.0807505249977112,
			53.95572885047577, -1.0807693004608154,
			53.95576041524595, -1.0807934403419495,
			53.95579040175545, -1.0808148980140686,
			53.95581407530036, -1.0808363556861877,
			53.95583459236174, -1.0808578133583069,
			53.9558630005839, -1.0808765888214111,
			53.955886674087594, -1.0808953642845154,
			53.95590719111325, -1.0809329152107239,
			53.955924551665525, -1.0809597373008728,
			53.9559419122106, -1.0809919238090515,
			53.95596242920905, -1.0810375213623047,
			53.95599241557329, -1.0810723900794983,
			53.955995572031426, -1.0811206698417664,
			53.95602082368793, -1.0811474919319153,
			53.956038184192906, -1.081179678440094,
			53.956061857597106, -1.081206500530243,
			53.956088687438985, -1.0812386870384216,
			53.95610920436523, -1.0812655091285706,
			53.95612498660933, -1.0812896490097046,
			53.95614392529434, -1.081305742263794,
			53.95616602041601, -1.081329882144928,
			53.956189693747646, -1.081356704235077,
			53.95620863240327, -1.0813701152801514,
			53.95623072749064, -1.0813862085342407,
			53.95625755722385, -1.0814210772514343,
			53.956284386939814, -1.081453263759613,
			53.95631595128939, -1.0814934968948364,
			53.95634120275185, -1.0815149545669556,
			53.95636961062886, -1.0815230011940002,
			53.95640433134113, -1.0815525054931638,
			53.95642800453746, -1.081576645374298,
			53.95645799056685, -1.0815981030464172,
			53.956487976574664, -1.0816141963005064,
			53.956521118979296, -1.081627607345581,
			53.95655741777316, -1.0816410183906555,
			53.956581090882565, -1.0816624760627747,
			53.95661738962424, -1.081675887107849,
			53.95665368833429, -1.0816892981529236,
			53.956686830607175, -1.081705391407013,
			53.95671523824868, -1.0817161202430725,
			53.95675153687356, -1.0817214846611023,
			53.95677363167324, -1.0817322134971619,
			53.95680519565245, -1.0817456245422363,
			53.9568493851832, -1.081753671169281,
			53.956884105495945, -1.0817617177963257,
			53.95692671674933, -1.0817670822143555,
			53.956966171574756, -1.0817509889602661,
			53.95699931359925, -1.0817509889602661,
			53.95703245559741, -1.0817644000053406,
			53.95707033213446, -1.081756353378296,
			53.95711136501079, -1.0817617177963257,
			53.957147663290776, -1.0817617177963257,
			53.95718711790713, -1.0817670822143555,
			53.957228150668506, -1.0817644000053406,
			53.957262870665815, -1.0817697644233704,
			53.95729601245449, -1.0817751288414001,
			53.957337045108694, -1.0817670822143555,
			53.957376499545816, -1.0817751288414001,
			53.95741437577033, -1.0817912220954895,
			53.95744909561258, -1.081801950931549,
			53.95747434638878, -1.0818180441856384,
			53.95750117532174, -1.0818475484848022,
			53.95751695703893, -1.081879734992981,
			53.95751537886749, -1.081777811050415,
			53.95752958240839, -1.0817080736160278,
			53.95755798947571, -1.081651747226715,
			53.95758955286112, -1.0815873742103577,
			53.957611647216694, -1.0815364122390747,
			53.957638476061305, -1.0814639925956726,
			53.9576700393858, -1.0814076662063599,
			53.95768424287403, -1.0813379287719727,
			53.9577110716719, -1.0812655091285706,
			53.95772685330966, -1.0812011361122131,
			53.9577442131043, -1.0811313986778257,
			53.95776946370175, -1.0810697078704832,
			53.957782088994755, -1.0809999704360962,
			53.95780418324829, -1.0809221863746643,
			53.957834168287945, -1.0808658599853516,
			53.95786415330605, -1.08077734708786,
			53.95789729461672, -1.0807049274444578,
			53.95793674852368, -1.0806673765182495,
			53.957973046085094, -1.0806351900100708,
			53.95801407807301, -1.0805761814117432,
			53.95805195371831, -1.0805439949035645,
			53.95808509487973, -1.0804930329322815,
			53.95813401749842, -1.0804608464241028,
			53.95817347118135, -1.080407202243805,
			53.958220815551584, -1.0803669691085815,
			53.95825395657878, -1.0803374648094177,
			53.95829656643217, -1.0803133249282837,
			53.95832970739911, -1.0802838206291197,
			53.95837705159195, -1.0802596807479858,
			53.95835022322262, -1.0802221298217771,
			53.95832812925843, -1.0801765322685242,
			53.95830761342407, -1.0801416635513306,
			53.95828394129496, -1.0801094770431519,
			53.95825869100908, -1.0800665616989136,
			53.95823344070792, -1.080029010772705,
			53.95820661224617, -1.079983413219452,
			53.95818294005969, -1.0799378156661987,
			53.95815295527087, -1.0798895359039307,
			53.95812454860896, -1.0798466205596924,
			53.958099298226536, -1.0798010230064392,
			53.958081938579774, -1.0797607898712158,
			53.95805984447341, -1.0797393321990967,
			53.958037750355366, -1.079704463481903,
			53.95802039068293, -1.0796722769737241,
			53.95800460915632, -1.0796400904655457,
			53.957988827623694, -1.0796132683753967,
			53.95796988977667, -1.0795891284942627,
			53.95795095192105, -1.0795596241950989,
			53.95793201405682, -1.0795247554779053,
			53.9579146543404, -1.0794979333877563,
			53.95789256014539, -1.0794684290885925,
			53.957876778570345, -1.0794201493263245,
			53.957845215402386, -1.0794013738632202,
			53.95782627749012, -1.079358458518982,
			53.95780733956926, -1.0793370008468628,
			53.95779313612299, -1.079304814338684,
			53.95776788553986, -1.0792753100395203,
			53.95773790045252, -1.079232394695282,
			53.957722118818964, -1.0792028903961182,
			53.95769371186347, -1.0791733860969543,
			53.95768424287403, -1.0791465640068054,
			53.95766688305443, -1.0791251063346863,
			53.95765110139397, -1.0790902376174927,
			53.95762585072482, -1.0790607333183286,
			53.95760533454489, -1.0790285468101501,
			53.95758797469244, -1.078999042510986,
			53.95757534934066, -1.0789722204208374,
			53.95755483313584, -1.0789427161216734,
			53.95752958240839, -1.0789185762405396,
			53.95751853521034, -1.0788890719413757,
			53.95749486263319, -1.078856885433197,
			53.9574711900426, -1.0788273811340332,
			53.957447517438574, -1.0787925124168396,
			53.957427001170856, -1.0787576436996458,
			53.95740648489306, -1.078709363937378,
			53.95737807772252, -1.0786744952201843,
			53.957360717775465, -1.0786503553390503,
			53.95734809235486, -1.0786154866218567,
			53.95731810696562, -1.078577935695648,
			53.95730705971152, -1.0785484313964844,
			53.95729443427468, -1.0785269737243652,
			53.95727707429277, -1.0785028338432312,
			53.957262870665815, -1.0784760117530823,
			53.957240776125424, -1.0784411430358887,
			53.957220259756006, -1.0784143209457397,
			53.95720921247597, -1.0783767700195312,
			53.95718238335513, -1.0783526301383972,
			53.957166601511275, -1.0783150792121887,
			53.957150819661464, -1.0782748460769653,
			53.95712083413035, -1.0782533884048462,
			53.957109786823985, -1.078234612941742,
			53.95709873951469, -1.0782158374786377,
			53.95707822307535, -1.078180968761444,
			53.95706086300342, -1.0781541466712952,
			53.957040346545455, -1.078132688999176,
			53.95702614283787, -1.078105866909027,
			53.95700404817203, -1.0780844092369077,
			53.95698668806921, -1.0780495405197144,
			53.95696774976698, -1.0780173540115356,
			53.95694565507021, -1.077985167503357,
			53.95692198216777, -1.0779610276222227,
			53.956898309251926, -1.0779181122779846,
			53.95687463632262, -1.077883243560791,
			53.9568493851832, -1.077856421470642,
			53.95683360321331, -1.0778269171714783,
			53.956814664841545, -1.0778027772903442,
			53.95680046105707, -1.0777786374092102,
			53.95677836627164, -1.0777518153190613,
			53.95675784967465, -1.0777249932289121,
			53.95674048946934, -1.0777035355567932,
			53.95671839465211, -1.0776767134666443,
			53.95670261263263, -1.0776391625404358,
			53.95668209599839, -1.0776203870773315,
			53.956656844742355, -1.077582836151123,
			53.956634749880806, -1.0775506496429443,
			53.95661423321318, -1.0775211453437805,
			53.95659371653542, -1.0774916410446167,
			53.956570043433175, -1.0774621367454529,
			53.95654637031749, -1.0774245858192444,
			53.95651796256093, -1.0773923993110657,
			53.95649271120551, -1.0773521661758423,
			53.956462725201106, -1.0773172974586487,
			53.95644378666087, -1.0772904753684998,
			53.956415378834414, -1.0772663354873657,
			53.95639170563092, -1.0772475600242615,
			53.95636803241397, -1.0772234201431274,
			53.95634120275185, -1.0771939158439636,
			53.95631437307248, -1.0771751403808592,
			53.95628596515784, -1.0771510004997253,
			53.95625282256631, -1.0771241784095764,
			53.956225992830056, -1.0770946741104126,
			53.95620074129782, -1.0770705342292786,
			53.95616444219343, -1.0770678520202637,
			53.95613603417663, -1.0770490765571594,
			53.956095000340454, -1.0770383477210999,
			53.95606659227635, -1.0770168900489807,
			53.95601924545983, -1.0770168900489807,
			53.95598136796793, -1.0770061612129211,
			53.95594822513425, -1.0770007967948914,
			53.95591508227427, -1.0769954323768616,
			53.95587720468771, -1.0769766569137573,
			53.95584090530165, -1.076960563659668,
			53.955799871175024, -1.076957881450653,
			53.955766728197105, -1.0769471526145935,
			53.95572885047577, -1.0769337415695188,
			53.955695707441414, -1.076928377151489,
			53.955662564380674, -1.0769149661064148,
			53.95561048237491, -1.076909601688385,
			53.95556471328579, -1.0769069194793701,
			53.955523678887346, -1.0769015550613403,
			53.95549369218602, -1.0768908262252808,
			53.95545581421668, -1.0768881440162659,
			53.95542582746652, -1.0768800973892212,
			53.95533271057865, -1.0768640041351318,
			53.95530430199518, -1.076858639717102,
			53.95526800211036, -1.0768532752990723,
			53.95523643696742, -1.0768452286720276,
			53.95521276309456, -1.0768425464630127,
			53.955185932689055, -1.076837182044983,
			53.95516068052692, -1.0768291354179382,
			53.95513700661103, -1.0768291354179382,
			53.95511491094409, -1.0768237709999084,
			53.95507861089438, -1.0768237709999084,
			53.955043889078155, -1.0768210887908936,
			53.95500285416713, -1.0768237709999084,
			53.95497602362654, -1.0768264532089233,
			53.95495077133729, -1.0768291354179382,
			53.95492394076322, -1.076837182044983,
			53.95489553190125, -1.0768398642539978,
			53.954862388204475, -1.076858639717102,
			53.95483871411923, -1.0768559575080872,
			53.95480872692529, -1.0768666863441465,
			53.9547818962598, -1.0768640041351318,
			53.954750330748865, -1.0768800973892212,
			53.9547171869367, -1.0768935084342957,
			53.95468719965536, -1.0768961906433105,
			53.95466352547065, -1.0769015550613403,
			53.95463827299214, -1.076909601688385,
			53.954617755342085, -1.076928377151489,
			53.95458303314207, -1.0769391059875488,
			53.95455304576426, -1.0769525170326233,
			53.954523058364884, -1.0769551992416382,
			53.954507275514445, -1.0769659280776978,
			53.95448044465498, -1.0769766569137573,
			53.95444887891584, -1.0769766569137573,
			53.95442678288425, -1.0769927501678467,
			53.95440153026236, -1.0770034790039062,
			53.95438101249581, -1.0770195722579956,
			53.95435102497273, -1.07703298330307,
			53.954330507181325, -1.0770490765571594,
			53.9543068327941, -1.0770705342292786,
			53.95428473668722, -1.0770893096923828,
			53.95425948397931, -1.0771295428276062,
			53.954242122733746, -1.07715904712677,
			53.954262640568636, -1.0771885514259338,
			53.95428158009957, -1.0772126913070679,
			53.95430209791505, -1.077234148979187,
			53.954321037428045, -1.0772636532783508,
			53.954346290098684, -1.0772985219955442,
			53.95436522959161, -1.0773387551307678,
			53.95437943420568, -1.0773763060569763,
			53.95440310855168, -1.0774111747741697,
			53.95441573486406, -1.077459454536438,
			53.954436252613505, -1.0775130987167358,
			53.954434674325455, -1.0775640606880188,
			53.95444887891584, -1.077609658241272,
			53.954461505214375, -1.0776740312576294,
			53.954475709795624, -1.0777384042739868,
			53.95447413150905, -1.077808141708374,
			53.9544678183622, -1.0778912901878357,
			53.954475709795624, -1.0779717564582825,
			53.95447886636857, -1.0780227184295652,
			53.95447413150905, -1.078057587146759,
			53.954475709795624, -1.0781031847000122,
			53.95447728808212, -1.0781380534172058,
			53.95447728808212, -1.078180968761444,
			53.954475709795624, -1.0782238841056824,
			53.95448044465498, -1.0782748460769653,
			53.954475709795624, -1.0783177614212036,
			53.95448044465498, -1.0783714056015015,
			53.95447728808212, -1.0784196853637695,
			53.954482022941306, -1.0784733295440674,
			53.95448360122757, -1.0785242915153503,
			53.95448360122757, -1.078580617904663,
			53.95449149265802, -1.0786396265029907,
			53.95449307094393, -1.0786959528923032,
			53.95449149265802, -1.0787469148635864,
			53.95449622751558, -1.0787898302078247,
			53.95450096237258, -1.0788515210151672,
			53.95450569722907, -1.0789158940315247,
			53.954512010370195, -1.078982949256897,
			53.9545246366496, -1.079031229019165,
			53.95452937150339, -1.0790875554084778,
			53.95454357606153, -1.0791385173797607,
			53.95454988919691, -1.0791921615600586,
			53.95455935889823, -1.0792431235313416,
			53.95456409374808, -1.0792726278305054,
			53.95457356344615, -1.0793074965476988,
			53.95458145485957, -1.0793396830558777,
			53.9545893462715, -1.0793772339820862,
			53.954597237681945, -1.0793986916542053,
			53.95460828565404, -1.0794308781623838,
			53.954619333623214, -1.0794657468795776,
			53.95463669471173, -1.0795113444328308,
			53.95464932095337, -1.0795435309410093,
			53.95465721235245, -1.0795891284942627,
			53.95465563407275, -1.0796159505844116,
			53.95467141686706, -1.0796535015106201,
			53.95467930826194, -1.0796937346458435
			};
	
	private Datastore datastore;
	public MovementThread(Datastore datastore) {
		this.datastore = datastore;
	}
	@Override
	public void run() {
		int i = 0;
		System.err.println(positions.length);
		while(true){
			try {
				double loclat = positions[i];
				i++;
				double loclong = positions[i];
				Drone drone = datastore.getDroneById(Message.C2_ID);
				drone.setLocLat(loclat);
				drone.setLocLong(loclong);
				broadcastLocation(loclat,loclong);
				//Do it like a drone
				ScanData scandata = SensorInterface.getDataForPoint(loclat, loclong);
				try{
					Broadcast.broadcast(scandata.toString());
				}catch(Exception e){
					System.out.println(e.getMessage());
				}
				
				//Do it like a dude 
				Thread.sleep(4000);
				i++;
				if (i == positions.length){
					i = 0;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	private void broadcastLocation(double loclat, double loclong) {
		StatusData message = new StatusData(Message.C2_ID, LocalDateTime.now(),loclat, loclong, 100.0, DroneState.IDLE, new double[0]);
		String messageString = message.toString();
		Broadcast.broadcast(messageString);
	}

}