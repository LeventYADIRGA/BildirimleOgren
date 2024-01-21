package com.lyadirga.bildirimleogren.data

data class Language(val wordOrSentence: String, val meaning: String)

fun getData(which: Int): Array<Language> {
    var data = week1
    when (which) {
        0 -> {
            data = week1
        }
        1 -> {
            data = week2
        }
        2 -> {
            data = week3
        }
        3 -> {
            data = week4
        }
        4 -> {
            data = week5
        }
        5 -> {
            data = week6
        }
        6 -> {
            data = lingusta1
        }
        7 -> {
            data = lingusta2
        }
        8 -> {
            data = lingusta3
        }
        9 -> {
            data = week7
        }
        else -> {
            println("Geçersiz seçenek.")
        }
    }
    return data
}

val week1 = arrayOf(
    Language("Unlikely","Muhtemel olmayan / beklenmeyen"),
    Language("At some point","Bir noktada"),
    Language("Craving","Özlem"),
    Language("I'm waiting for her","Onu bekliyorum"),
    Language("Tom is not here","Tom burada değil"),
    Language("wall","duvar"),
    Language("Although","Yine de"),
    Language("just a minute / second","Bir dakika bekle"),
    Language("I am serious","Ben ciddiyim"),
    Language("By the way","Bu arada"),
    Language("Don't mention it","Önemli değil / Ne demek / Sıkıntı yok"),
    Language("Right away!","Hemen / Derhal"),
    Language("got it","anladım"),
    Language("In fact","Aslında"),
    Language("instead","yerine"),
    Language("So in summary","Yani özetle"),
    Language("either","herhangi biri"),
    Language("I hate","Nefret ediyorum"),
    Language("Skin","Deri"),
    Language("I think so","Sanırım öyle / Ben de öyle düşünüyorum"),
    Language("at least","en azından"),
    Language("So","Bu yüzden"),
    Language("Incredible / Amazing / Unbelievable","İnanılmaz"),
    Language("I always wonder","Her zaman merak ediyorum / Hep merak ederim"),
    Language("I know none","Hiçbirini bilmiyorum"),
    Language("Even a kid","Bir çocuk bile"),
    Language("Let me help","İzin ver yardımcı olayım"),
    Language("Worth it","Buna değer"),
    Language("To be honest","Dürüst olmak gerekirse"),
    Language("You know why","Nedenini biliyorsun")
)

val week2 = arrayOf(
    Language("... is useless","işe yaramaz"),
    Language("Neither","Hiçbiri"),
    Language("I prefer neither pink nor purple","Ne pembeyi ne de moru tercih ederim"),
    Language("I prefer either pink or purple","Pembe ya da moru tercih ederim"),
    Language("I am not any of them","Ben hiçbiri değilim"),
    Language("I am none of them","Ben hiçbiri değilim"),
    Language("slippers","terlik"),
    Language("Don't mind","Kafana takma"),
    Language("Never mind","Boşver / kafana takma"),
    Language("soon","yakında"),
    Language("as","olarak"),
    Language("yet","ama (diğer bir anlamı)"),
    Language("Se tou for now","Şimdilik görüşürüz"),
    Language("It's ok for me","Benim için uygun"),
    Language("I've seen that","Bunu gördüm"),
    Language("than","başta - sonra / sonunda - o zaman"),
    Language("How may I help you?","Size nasıl yardım edebilirm?"),
    Language("till noon","öğlene kadar"),
    Language("How lucky I am","Ne kadar şanslıyım"),
    Language("I'm all ears","Can kulağı ile dinliyorum"),
    Language("I hope you get better soon","Umarım en kısa zamanda iyi olursun"),
    Language("Do you mind?","Sakıncası var mı?"),
    Language("Anyways","Herneyse"),
    Language("Let me","İzin ver bana"),
    Language("I see","Anlıyorum"),
    Language("Upset","Üzgün"),
    Language("several","bir kaç kez"),
    Language("One of my friends","Arkadaşlarımdan biri"),
    Language("Probably / Likely","Muhtemelen"),
    Language("Such as / For example","Örneğin")
)

val week3 = arrayOf(
    Language("workout","antrenman yapmak"),
    Language("Like what?","Ne gibi?"),
    Language("but also","ama aynı zamanda"),
    Language("I am so busy","çok meşgulüm"),
    Language("Don't worry at all","Hiç endişelenme"),
    Language("Calm down!","Sakin ol!"),
    Language("at least","en az"),
    Language("at most","en fazla"),
    Language("crosswalk","yaya geçidi"),
    Language("You'd better hurry up","Acele etsen iyi olur"),
    Language("in time","zamanında"),
    Language("Guess what?","Ne oldu?"),
    Language("Reason","Sebep"),
    Language("Cool!","Havalı!"),
    Language("Proud","Gurur"),
    Language("Good point","İyi bir nokta"),
    Language("You've made a good point","İyi bir noktaya parmak bastın"),
    Language("a couple of","birkaç tane"),
    Language("must fine them","onlara ceza (para cezası) vermeli"),
    Language("You mean that","Bunu demek istiyorsun"),
    Language("near here","buraya yakın"),
    Language("exist","var olmak"),
    Language("Water exists in three states: solid, liquid, and gas","Su üç halde varlığını sürdürür (bulunur): katı, sıvı ve gaz"),
    Language("Guess / my guess","Tahmin / tahminimce"),
    Language("guest","misafir"),
    Language("exciting","heyacan verici"),
    Language("at all","hiç"),
    Language("It's not difficult at all","Hiç zor değil"),
    Language("be patient","sabırlı ol / patient aynı zamanda hasta demek"),
    Language("I don't think so","Öyle olduğunu düşünmüyorum")
)

val week4 = arrayOf(
    Language("As for as I know","Bildiğim kadarıyla"),
    Language("I'd love to","memnuniyetle yaparım"),
    Language("Would you like to join us for dinner tonight? I'd love to!","Bu akşam bizimle akşam yemeğine gelmek ister misin? Memnuniyetle!"),
    Language("I have to","Mecburum / Yapmak zorundayım"),
    Language("as it is","bu haliyle / olduğu gibi"),
    Language("Almost","Neredeyse"),
    Language("Waterproof / fireproof / foolproof","su geçirmez / yakmaz / kusursuz"),
    Language("However / but","Ancak / Fakat"),
    Language("Don't forget to let me know","Bana haber vermeyi unutma"),
    Language("Isn't it?","Öyle değil mi?"),
    Language("beforehand","daha önce / önceden"),
    Language("That is all!","Hepsi bu kadar"),
    Language("headache","baş ağrısı"),
    Language("application","başvuru"),
    Language("promote","terfi"),
    Language("resign","istifa etmek"),
    Language("glad","memnun"),
    Language("No need thank you","Teşekküre gerek yok"),
    Language("recent years","son yıllar"),
    Language("rush hour","yoğun saat"),
    Language("I left the chat","sohbetten ayrıldım"),
    Language("which is called","şu şekilde adlandırılır / buna denir"),
    Language("Suddenly","Aniden"),
    Language("realized","gerçekleştirilmiş / farkına varmak"),
    Language("exact time","tam zamanı"),
    Language("damn","lanet olsun"),
    Language("kidding me","benimle dalga geçiyor"),
    Language("are you kidding me?","Benimle dalga mı geçiyorsun?"),
    Language("rush","acele etmek"),
    Language("I need to rush to catch the bus","Otobüsü yakalamak için acele etmem gerekiyor")
)

val week5 = arrayOf(
    Language("Hi there","Merhaba"),
    Language("on time","zamanında"),
    Language("The train arrived on time, and we were able to catch our connecting flight","Tren belirlenen saatte geldi ve bağlantı uçuşumuzu yakalayabildik"),
    Language("I'll do you best","sana (elimden gelenin) en iyisini yapacağım"),
    Language("try hard","çok çabalamak"),
    Language("Although the puzzle was difficult, she decided to try hard to solve it","Puzzle zordu ama o, onu çözmek için çok çaba harcamaya karar verdi"),
    Language("yummy","lezzetli"),
    Language("yummy / tasty",""),
    Language("Then, I should try","O halde denemeliyim"),
    Language("as long as","bu şartla / bu takdirde"),
    Language("You can borrow my car as long as you promise to drive carefully","Aracımı ödünç alabilirsin, yeter ki dikkatli sürmeyi taahhüt et"),
    Language("That's ok","sorun yok (birisi özür dilediğinde)"),
    Language("It's ok","sorun değil (teselli verme, olayı küçümseme)"),
    Language("What will you do?","Ne yapacaksın?"),
    Language("What am I talking about?","Neden bahsediyorum?"),
    Language("replacement","yenisiyle değiştirme"),
    Language("After my phone was damaged, I had to get a replacement","Telefonum hasar gördükten sonra, bir değiştirme almam gerekti"),
    Language("improvements","iyileştirmeler"),
    Language("The latest software update includes several performance improvements and bug fixes","Son yazılım güncellemesi, birkaç performans iyileştirmesi ve hata düzeltmesi içermektedir"),
    Language("worth your while","vakit ayırmaya değer"),
    Language("consistent","tutarlı"),
    Language("inconsistent","tutarsız"),
    Language("most likely","büyük ihtimalle"),
    Language("produce","üretmek"),
    Language("That's incredible","Bu inanılmaz"),
    Language("wish","dilek / dilemek"),
    Language("ordinary","sıradan"),
    Language("extraordinary","olağanüstü"),
    Language("discover","keşfetmek"),
    Language("The scientist made an exciting discovery while conducting experiments in the laboratory","Bilim adamı laboratuvarda deneyler yaparken heyecan verici bir keşif yaptı")
)

val week6 = arrayOf(
    Language("That's terrible","Bu korkunç"),
    Language("I want to try","Denemek istiyorum"),
    Language("I remember you","Seni hatırlıyorum"),
    Language("Upparently so","Öyle görünüyor / Görünüşe göre öyle"),
    Language("I think so","Bence öyle"),
    Language("vital","çok önemli / hayati"),
    Language("charming","çok şeker / cazibeli / alımlı"),
    Language("even if / even so","olsa bile"),
    Language("apology","özür"),
    Language("honest","dürüst"),
    Language("What do you mean?","Ne demek istiyorsun?"),
    Language("What do you think?","Ne düşünüyorsun?"),
    Language("What should I do?","Ne yapmalıyım?"),
    Language("They'll leave without me","Bensiz gidecekler"),
    Language("Easy!","Sakin ol!"),
    Language("I have to go","Gitmem gerek"),
    Language("Was it?","O mu?"),
    Language("Saw's right","Saw haklı"),
    Language("Are you crazy?","Aklını mı kaçırdın?"),
    Language("It will be all right","Herşey yoluna girecek"),
    Language("currently","şu anda"),
    Language("What is this?","Bu nedir?"),
    Language("close","yakın"),
    Language("for now","şimdilik"),
    Language("one more","bir daha"),
    Language("I don't think so","Öyle düşünmüyorum"),
    Language("I din't know that","Bunu bilmiyordum"),
    Language("reptile","sürüngen"),
    Language("I din't have task","Görevim yoktu"),
    Language("Kindergarten","Anaokulu")
)

val lingusta1 = arrayOf(
    Language("ğkp","kğ"),
    Language("",""),
    Language("",""),
    Language("","")
)

val lingusta2 = arrayOf(
    Language("ğkp","kğ"),
    Language("",""),
    Language("",""),
    Language("","")
)

val lingusta3 = arrayOf(
    Language("ğkp","kğ"),
    Language("",""),
    Language("",""),
    Language("","")
)

val week7 = arrayOf(
    Language("I remember", "Ben hatırlıyorum"),
    Language("alive", "canlı"),
    Language("choke", "boğmak"),
    Language("I meet a co-worker", "Bir iş arkadaşımla tanıştım"),
    Language("I enjoy listening to music", "Müzik dinlemek hoşuma gidiyor"),
    Language("I like software development", "Yazılım geliştirmeyi seviyorum"),
    Language("I'm doing well, thank you. How about you?", "İyiyim, teşekkürler. Peki ya sen?")
)



