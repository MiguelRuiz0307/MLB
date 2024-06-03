package com.example.mlbtheshow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.example.mlbtheshow.ui.theme.MLBTheShowTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog


/**
 * La actividad principal de la aplicación que actúa como el punto de entrada.
 * Extiende ComponentActivity y configura la UI utilizando Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    // Declaración tardía del ViewModel de favoritos
    private lateinit var favoritosViewModel: FavoritosViewModel

    /**
     * Método onCreate llamado cuando se crea la actividad.
     * Configura el ViewModel y la UI de la aplicación.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Establecer la pantalla completa
        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                        or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // Inicialización del ViewModel de favoritos utilizando ViewModelProvider
        favoritosViewModel = ViewModelProvider(this).get(FavoritosViewModel::class.java)

        // Habilita el diseño de borde a borde en la UI
        enableEdgeToEdge()

        // Configuración del contenido de la actividad utilizando Jetpack Compose
        setContent {
            MLBTheShowTheme {
                // Llama a la función MyApp pasándole el ViewModel de favoritos
                MyApp(favoritosViewModel)
            }
        }
    }
}




/**
 * Clase de datos que representa un equipo en la aplicación.
 *
 * @param nombre El nombre del equipo.
 * @param informacion Información adicional sobre el equipo.
 * @param imagen El recurso de imagen del equipo.
 * @param esFavorito El estado de favorito del equipo, inicializado como falso por defecto.
 */
data class Equipo(
    val nombre: String,
    val informacion: String,
    val imagen: Int,
    val detalles: String = "", // Valor predeterminado para el campo detalles
    var esFavorito: MutableState<Boolean> = mutableStateOf(false)
)




/**
 * Función composable principal que configura la navegación de la aplicación.
 * @param favoritosViewModel El ViewModel que gestiona la lógica de favoritos.
 */
@Composable
fun MyApp(favoritosViewModel: FavoritosViewModel) {
    // Recordamos y creamos una instancia del controlador de navegación
    val navController = rememberNavController()

    // Configuramos el host de navegación y definimos las rutas de navegación
    NavHost(navController = navController, startDestination = "welcome") {
        // Ruta para la pantalla de bienvenida
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }
        // Ruta para la pantalla de exploración
        composable("explore") {
            ExploreScreen(navController = navController)
        }
        // Ruta para la pantalla de la liga americana
        composable("pantalla_americana") {
            PantallaAmericana(navController = navController, favoritosViewModel = favoritosViewModel)
        }
        // Ruta para la pantalla de la liga nacional
        composable("pantalla_nacional") {
            PantallaNacional(navController = navController, favoritosViewModel = favoritosViewModel)
        }
        // Ruta para la pantalla de favoritos
        composable("favoritos") {
            PantallaFavoritos(navController, favoritosViewModel = favoritosViewModel)
        }
        composable("equipo_detail/{equipoNombre}") { backStackEntry ->
            val equipoNombre = backStackEntry.arguments?.getString("equipoNombre")
            val equipo = favoritosViewModel.todosLosEquipos.value?.find { it.nombre == equipoNombre }
            equipo?.let {
                EquipoDetailScreen(navController, it)
            }
        }
    }
}




/**
 * ViewModel para gestionar la lista de equipos favoritos.
 */
class FavoritosViewModel : ViewModel() {

    // Lista mutable de equipos favoritos
    private val _equiposFavoritos = MutableLiveData<List<Equipo>>()

    private val _todosLosEquipos = MutableLiveData<List<Equipo>>()

    // Exponemos una LiveData inmutable para que otras clases solo puedan observarla, no modificarla directamente
    val todosLosEquipos: LiveData<List<Equipo>> = _todosLosEquipos
    val equiposFavoritos: LiveData<List<Equipo>> = _equiposFavoritos

    private val _showMessage = MutableStateFlow(false)
    val showMessage: StateFlow<Boolean> = _showMessage
    var lastAction: Action? = null

    // Enumeración para rastrear las acciones
    enum class Action {
        AGREGAR,
        ELIMINAR
    }

    init {
        // Inicializamos la lista de favoritos vacía
        _equiposFavoritos.value = emptyList()
        _todosLosEquipos.value = listOf(
            Equipo(
                "Baltimore Orioles",
                "Fueron fundados en 1894\nHan ganado nueve series Mundiales",
                R.drawable.orioles,
                "Los Baltimore Orioles (en español, Los Orioles de Baltimore) son un equipo de béisbol profesional de los Estados Unidos con sede en Baltimore, Maryland. " +
                        "Compiten en la División Este de la Liga Americana (AL) de las Grandes Ligas de Béisbol (MLB) y disputan sus partidos como locales en el Oriole Park at Camden Yards.\n" +
                        "Durante su historia ha logrado tres Series Mundiales, siete campeonatos de la Liga Americana y diez títulos de división."
            ),
            Equipo(
                "New York Yankees",
                "Fueron fundados en 1903\nHan ganado 27 series Mundiales",
                R.drawable.yankees,
                "Los New York Yankees son uno de los equipos más emblemáticos y exitosos de la Major League Baseball (MLB). Fundados en 1901, han ganado 27 Series Mundiales, más que cualquier otro equipo en la historia de la MLB. " +
                        "Los Yankees juegan en el Yankee Stadium, ubicado en el Bronx, Nueva York. El equipo ha contado con algunas de las mayores leyendas del béisbol, como Babe Ruth, Lou Gehrig, Joe DiMaggio, Mickey Mantle y Derek Jeter. " +
                        "Su uniforme con rayas y el famoso logo 'NY' son reconocidos mundialmente. A lo largo de los años, los Yankees han establecido una tradición de excelencia y una gran base de seguidores."
            ),
            Equipo(
                "Boston Red Sox",
                "Fueron fundados en 1901\nHan ganado 9 Series Mundiales",
                R.drawable.boston,
                "Los Boston Red Sox son un equipo de béisbol profesional de los Estados Unidos con sede en Boston, Massachusetts. Compiten en la División Este de la Liga Americana (AL) de las Grandes Ligas de Béisbol (MLB). " +
                        "El equipo ha ganado nueve Series Mundiales, con su primer título en 1903 y el más reciente en 2018. Su estadio local es el Fenway Park, conocido por su icónica pared 'Green Monster' en el jardín izquierdo."
            ),
            Equipo(
                "Tampa Bay Rays",
                "Fueron fundados en 1998\nAún no han ganado una serie Mundial.",
                R.drawable.rays,
                "Los Tampa Bay Rays son un equipo de béisbol profesional de los Estados Unidos con sede en St. Petersburg, Florida. Compiten en la División Este de la Liga Americana (AL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1998, los Rays han llegado a la Serie Mundial en dos ocasiones, en 2008 y 2020, pero aún no han conseguido ganar el campeonato."
            ),
            Equipo(
                "Toronto Blue Jays",
                "Fueron fundados en 1977\nHan ganado dos veces la Serie Mundial",
                R.drawable.toronto,
                "Los Toronto Blue Jays son un equipo de béisbol profesional de Canadá con sede en Toronto, Ontario. Son el único equipo canadiense en las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1977, los Blue Jays han ganado la Serie Mundial en dos ocasiones, en 1992 y 1993. Juegan sus partidos como locales en el Rogers Centre, uno de los estadios más modernos de la MLB."
            ),
            Equipo(
                "Chicago White Sox",
                "Fueron fundados en 1901\nHan ganado tres series Mundiales",
                R.drawable.whitesox,
                "Los Chicago White Sox, conocidos también como los 'Pale Hose' o 'South Siders', son un equipo de béisbol profesional de los Estados Unidos con sede en Chicago, Illinois. " +
                        "Compiten en la División Central de la Liga Americana (AL) de las Grandes Ligas de Béisbol (MLB). Han ganado la Serie Mundial en tres ocasiones, en 1906, 1917 y 2005."
            ),
            Equipo(
                "Cleveland Guardians",
                "Fueron fundados en 1894\nHan ganado dos series Mundiales",
                R.drawable.cleveland,
                "Los Cleveland Guardians, anteriormente conocidos como los Cleveland Indians, son un equipo de béisbol profesional de los Estados Unidos con sede en Cleveland, Ohio. " +
                        "Compiten en la División Central de la Liga Americana (AL) de las Grandes Ligas de Béisbol (MLB). Han ganado la Serie Mundial en dos ocasiones, en 1920 y 1948."
            ),
            Equipo(
                "Detroit Tigers",
                "Fueron fundados en 1894\nHan ganado cuatro Series Mundiales",
                R.drawable.detroit,
                "Los Detroit Tigers son un equipo de béisbol profesional de los Estados Unidos con sede en Detroit, Michigan. Compiten en la División Central de la Liga Americana (AL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1894, los Tigers han ganado la Serie Mundial en cuatro ocasiones, en 1935, 1945, 1968 y 1984."
            ),
            Equipo(
                "Kansas City Royals",
                "Fueron fundados en 1969\nHan ganado dos series Mundiales",
                R.drawable.kansascity,
                "Los Kansas City Royals son un equipo de béisbol profesional de los Estados Unidos con sede en Kansas City, Missouri. Compiten en la División Central de la Liga Americana (AL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1969, los Royals han ganado la Serie Mundial en dos ocasiones, en 1985 y 2015."
            ),
            Equipo(
                "Minnesota Twins",
                "Fueron fundados en 1901\nHan ganado tres series Mundiales",
                R.drawable.twins,
                "Los Minnesota Twins son un equipo de béisbol profesional de los Estados Unidos con sede en Minneapolis, Minnesota. Compiten en la División Central de la Liga Americana (AL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1901 como los Washington Senators, se trasladaron a Minnesota en 1961. Han ganado la Serie Mundial en tres ocasiones, en 1924, 1987 y 1991."
            ),
            Equipo(
                "Los Ángeles Angels",
                "Fueron fundados en 1961\nHan ganado una serie Mundial",
                R.drawable.angels,
                "Los Los Ángeles Angels, conocidos oficialmente como los Los Angeles Angels of Anaheim, son un equipo de béisbol profesional de los Estados Unidos con sede en Anaheim, California. " +
                        "Compiten en la División Oeste de la Liga Americana (AL) de las Grandes Ligas de Béisbol (MLB). Fundados en 1961, ganaron su única Serie Mundial en 2002."
            ),
            Equipo(
                "Seattle Mariners",
                "Fueron fundados en 1977\nAún no han ganado una serie Mundial",
                R.drawable.mariners,
                "Los Seattle Mariners son un equipo de béisbol profesional de los Estados Unidos con sede en Seattle, Washington. Compiten en la División Oeste de la Liga Americana (AL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1977, los Mariners aún no han ganado una Serie Mundial. Su estadio local es el T-Mobile Park, conocido por su techo retráctil y vistas del horizonte de Seattle."
            ),
            Equipo(
                "Houston Astros",
                "Fueron fundados en 1962\nHan ganado dos series Mundiales",
                R.drawable.astros,
                "Los Houston Astros son un equipo de béisbol profesional de los Estados Unidos con sede en Houston, Texas. Compiten en la División Oeste de la Liga Americana (AL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1962, los Astros han ganado la Serie Mundial en dos ocasiones, en 2017 y 2022."
            ),
            Equipo(
                "Oakland Athletics",
                "Fueron fundados en 1901\nHan ganado nueve series Mundiales",
                R.drawable.athletics,
                "Los Oakland Athletics, conocidos también como los 'A's', son un equipo de béisbol profesional de los Estados Unidos con sede en Oakland, California. " +
                        "Compiten en la División Oeste de la Liga Americana (AL) de las Grandes Ligas de Béisbol (MLB). Fundados en 1901 como los Philadelphia Athletics, se trasladaron a Oakland en 1968. " +
                        "Han ganado la Serie Mundial en nueve ocasiones, con su primer título en 1910 y el más reciente en 1989."
            ),
            Equipo(
                "Texas Rangers",
                "Fueron fundados en 1961\nAún no han ganado una serie Mundial",
                R.drawable.rangers,
                "Los Texas Rangers son un equipo de béisbol profesional de los Estados Unidos con sede en Arlington, Texas. Compiten en la División Oeste de la Liga Americana (AL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1961 como los Washington Senators, se trasladaron a Texas en 1972. A pesar de haber llegado a la Serie Mundial en 2010 y 2011, los Rangers aún no han conseguido ganar el campeonato."
            ),
            Equipo(
                "Atlanta Braves",
                "Fueron fundados en 1871\nHan ganado cuatro series Mundiales",
                R.drawable.braves,
                "Los Atlanta Braves son un equipo de béisbol profesional de los Estados Unidos con sede en Atlanta, Georgia. Compiten en la División Este de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1871 como los Boston Red Stockings, se trasladaron a Atlanta en 1966. Han ganado la Serie Mundial en cuatro ocasiones, en 1914, 1957, 1995 y 2021."
            ),
            Equipo(
                "Miami Marlins",
                "Fueron fundados en 1993\nHan ganado dos series Mundiales",
                R.drawable.marlins,
                "Los Miami Marlins son un equipo de béisbol profesional de los Estados Unidos con sede en Miami, Florida. Compiten en la División Este de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1993, los Marlins han ganado la Serie Mundial en dos ocasiones, en 1997 y 2003."
            ),
            Equipo(
                "New York Mets",
                "Fueron fundados en 1962\nHan ganado dos series Mundiales",
                R.drawable.mets,
                "Los New York Mets son un equipo de béisbol profesional de los Estados Unidos con sede en Queens, Nueva York. Compiten en la División Este de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1962, los Mets han ganado la Serie Mundial en dos ocasiones, en 1969 y 1986."
            ),
            Equipo(
                "Philadelphia Phillies",
                "Fueron fundados en 1883\nHan ganado dos series Mundiales",
                R.drawable.phillies,
                "Los Philadelphia Phillies son un equipo de béisbol profesional de los Estados Unidos con sede en Filadelfia, Pensilvania. Compiten en la División Este de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1883, los Phillies han ganado la Serie Mundial en dos ocasiones, en 1980 y 2008."
            ),
            Equipo(
                "Washington Nationals",
                "Fueron fundados en 1969\nHan ganado una serie Mundial",
                R.drawable.nationals,
                "Los Washington Nationals son un equipo de béisbol profesional de los Estados Unidos con sede en Washington, D.C. Compiten en la División Este de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1969 como los Montreal Expos, se trasladaron a Washington en 2005. Ganaron su única Serie Mundial en 2019."
            ),
            Equipo(
                "Chicago Cubs",
                "Fueron fundados en 1876\nHan ganado tres series Mundiales",
                R.drawable.cubs,
                "Los Chicago Cubs son un equipo de béisbol profesional de los Estados Unidos con sede en Chicago, Illinois. Compiten en la División Central de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1876, los Cubs han ganado la Serie Mundial en tres ocasiones, en 1907, 1908 y 2016. Su estadio local es el histórico Wrigley Field."
            ),
            Equipo(
                "Cincinnati Reds",
                "Fueron fundados en 1881\nHan ganado cinco series Mundiales",
                R.drawable.reds,
                "Los Cincinnati Reds son un equipo de béisbol profesional de los Estados Unidos con sede en Cincinnati, Ohio. Compiten en la División Central de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1881, los Reds han ganado la Serie Mundial en cinco ocasiones, en 1919, 1940, 1975, 1976 y 1990."
            ),
            Equipo(
                "Milwaukee Brewers",
                "Fueron fundados en 1969\nAún no han ganado una serie Mundial",
                R.drawable.brewers,
                "Los Milwaukee Brewers son un equipo de béisbol profesional de los Estados Unidos con sede en Milwaukee, Wisconsin. Compiten en la División Central de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1969, los Brewers aún no han ganado una Serie Mundial, aunque llegaron al campeonato en 1982."
            ),
            Equipo(
                "Pittsburgh Pirates",
                "Fueron fundados en 1882\nHan ganado cinco series Mundiales",
                R.drawable.pirates,
                "Los Pittsburgh Pirates son un equipo de béisbol profesional de los Estados Unidos con sede en Pittsburgh, Pensilvania. Compiten en la División Central de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1882, los Pirates han ganado la Serie Mundial en cinco ocasiones, en 1909, 1925, 1960, 1971 y 1979."
            ),
            Equipo(
                "St. Louis Cardinals",
                "Fueron fundados en 1882\nHan ganado once series Mundiales",
                R.drawable.cardinals,
                "Los St. Louis Cardinals son un equipo de béisbol profesional de los Estados Unidos con sede en San Luis, Misuri. Compiten en la División Central de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1882, los Cardinals han ganado la Serie Mundial en once ocasiones, la segunda mayor cantidad en la historia de la MLB. Sus títulos más recientes fueron en 2006 y 2011."
            ),
            Equipo(
                "Arizona Diamondbacks",
                "Fueron fundados en 1998\nHan ganado una serie Mundial",
                R.drawable.arizona,
                "Los Arizona Diamondbacks, conocidos también como los 'D-backs', son un equipo de béisbol profesional de los Estados Unidos con sede en Phoenix, Arizona. Compiten en la División Oeste de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1998, ganaron su única Serie Mundial en 2001."
            ),
            Equipo(
                "Colorado Rockies",
                "Fueron fundados en 1993\nAún no han ganado una serie Mundial",
                R.drawable.rockies,
                "Los Colorado Rockies son un equipo de béisbol profesional de los Estados Unidos con sede en Denver, Colorado. Compiten en la División Oeste de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1993, los Rockies aún no han ganado una Serie Mundial, aunque llegaron al campeonato en 2007."
            ),
            Equipo(
                "Los Angeles Dodgers",
                "Fueron fundados en 1883\nHan ganado siete series Mundiales",
                R.drawable.dodgers,
                "Los Los Angeles Dodgers son un equipo de béisbol profesional de los Estados Unidos con sede en Los Ángeles, California. Compiten en la División Oeste de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1883 como los Brooklyn Atlantics, se trasladaron a Los Ángeles en 1958. Han ganado la Serie Mundial en siete ocasiones, la más reciente en 2020."
            ),
            Equipo(
                "San Diego Padres",
                "Fueron fundados en 1969\nAún no han ganado una serie Mundial",
                R.drawable.padres,
                "Los San Diego Padres son un equipo de béisbol profesional de los Estados Unidos con sede en San Diego, California. Compiten en la División Oeste de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1969, los Padres aún no han ganado una Serie Mundial, aunque han llegado al campeonato en dos ocasiones, en 1984 y 1998."
            ),
            Equipo(
                "San Francisco Giants",
                "Fueron fundados en 1883\nHan ganado ocho series Mundiales",
                R.drawable.giants,
                "Los San Francisco Giants son un equipo de béisbol profesional de los Estados Unidos con sede en San Francisco, California. Compiten en la División Oeste de la Liga Nacional (NL) de las Grandes Ligas de Béisbol (MLB). " +
                        "Fundados en 1883 como los New York Gothams, se trasladaron a San Francisco en 1958. Han ganado la Serie Mundial en ocho ocasiones, incluyendo tres títulos recientes en 2010, 2012 y 2014."
            )
        )
    }

    // Método para establecer los equipos
    fun setTodosLosEquipos(equipos: List<Equipo>) {


    }

    /**
     * Método para agregar un equipo a la lista de favoritos.
     * @param equipo El equipo a agregar a la lista de favoritos.
     */
    fun agregarEquipoFavorito(equipo: Equipo) {
        val listaActual = _equiposFavoritos.value ?: emptyList()
        // Verificar si el equipo ya está en la lista de favoritos
        if (!listaActual.contains(equipo)) {
            val nuevaLista = listaActual.toMutableList()
            nuevaLista.add(equipo)
            _equiposFavoritos.postValue(nuevaLista) // Utilizar postValue para actualizar el valor de LiveData
            _showMessage.value = true // Mostrar mensaje de confirmación
            lastAction = Action.AGREGAR // Actualizar la última acción realizada
        }
    }

    /**
     * Método para eliminar un equipo de la lista de favoritos.
     * @param equipo El equipo a eliminar de la lista de favoritos.
     */
    fun eliminarEquipoFavorito(equipo: Equipo) {
        val listaActual = _equiposFavoritos.value ?: return
        val nuevaLista = listaActual.toMutableList()
        nuevaLista.remove(equipo)
        _equiposFavoritos.value = nuevaLista
        _showMessage.value = true // Mostrar mensaje de confirmación
        lastAction = Action.ELIMINAR // Actualizar la última acción realizada
    }

    /**
     * Método para mostrar u ocultar el mensaje de confirmación.
     * @param mostrar Booleano que indica si se debe mostrar el mensaje de confirmación.
     */
    fun mostrarMensajeDeConfirmacion(mostrar: Boolean) {
        _showMessage.value = mostrar
    }


    fun searchTeams(query: String) {
        val filteredTeams = _todosLosEquipos.value?.filter { equipo ->
            equipo.nombre.contains(query, ignoreCase = true)
        }
        _equiposFavoritos.value = filteredTeams ?: emptyList()
    }
}




/**
 * Pantalla de bienvenida que muestra una imagen de fondo, un mensaje de bienvenida
 * y un botón para explorar la aplicación.
 *
 * @param navController Controlador de navegación para gestionar la navegación entre pantallas.
 */
@Composable
fun WelcomeScreen(navController: NavController) {
    // Caja principal que ocupa toda la pantalla
    Box(modifier = Modifier.fillMaxSize()) {

        // Muestra una imagen que ocupa toda la pantalla
        DisplayImage(modifier = Modifier.fillMaxSize())

        // Caja secundaria que superpone una capa negra semitransparente sobre la imagen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.8f }
                .background(Color.Black)
        )

        // Columna centrada horizontal y verticalmente para mostrar el mensaje de bienvenida
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Columna interna para alinear el texto de bienvenida
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Texto de bienvenida
                Greeting(
                    text = "Bienvenido a",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Nombre de la aplicación
                Greeting(
                    text = "MLB THE SHOW",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Botón para explorar la aplicación, ubicado en la parte inferior central de la pantalla
        ExploreButton(
            onClick = { navController.navigate("explore") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp) // Ajusta la distancia entre el botón y el borde inferior
        )
    }
}




/**
 * Pantalla de exploración que muestra opciones para navegar a la Liga Americana y Liga Nacional,
 * así como accesos a favoritos y elementos eliminados.
 *
 * @param navController Controlador de navegación para gestionar la navegación entre pantallas.
 */
@Composable
fun ExploreScreen(navController: NavController) {
    // Caja principal que ocupa toda la pantalla con un fondo de color marrón
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8B4513))
    ) {
        // Cabecera con imagen de fondo, icono de flecha hacia atrás y título
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            // Imagen de fondo en la cabecera
            Image(
                painter = painterResource(id = R.drawable.menu),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )

            // Caja interior para el contenido de la cabecera (flecha y título)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                // Botón de flecha hacia atrás
                IconButton(
                    onClick = { navController.navigate("welcome") }, // Navegar de nuevo a la pantalla de bienvenida
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Título centrado
                Text(
                    text = "MLB THE SHOW",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Column para los botones y las imágenes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 150.dp)  // Ajusta la distancia desde la parte superior
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fila que contiene las columnas con los botones e imágenes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Columna para el botón y la imagen de la Liga Americana
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.americana),
                        contentDescription = null,
                        modifier = Modifier
                            .clickable { navController.navigate("pantalla_americana") }
                            .width(100.dp)  // Ajusta el ancho de la imagen
                            .height(100.dp)  // Ajusta la altura de la imagen
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(16.dp))  // Bordes redondeados
                    )
                }

                // Columna para el botón y la imagen de la Liga Nacional
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.nacional),
                        contentDescription = null,
                        modifier = Modifier
                            .clickable { navController.navigate("pantalla_nacional") }
                            .width(100.dp)  // Ajusta el ancho de la imagen
                            .height(100.dp)  // Ajusta la altura de la imagen
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(16.dp))  // Bordes redondeados
                    )
                }
            }

            // Espacio entre la fila de botones y la imagen centrada
            Spacer(modifier = Modifier.height(16.dp))

            // Imagen centrada debajo de los botones
            Image(
                painter = painterResource(id = R.drawable.iconopantalla),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Ocupa el 80% del ancho disponible
                    .padding(horizontal = 17.dp, vertical = 40.dp) // Ajusta el padding
                    .aspectRatio(1.5f) // Relación de aspecto de la imagen (opcional)
                    .align(Alignment.CenterHorizontally)
            )

        }

        // Barra de navegación en la parte inferior de la pantalla
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.Black)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            // Fila para los iconos de la barra de navegación
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Botón de favoritos
                IconButton(onClick = { navController.navigate("favoritos") }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorites",
                        tint = Color.White
                    )
                }
            }
        }
    }
}





/**
 * Botón personalizado que muestra un texto centrado y ejecuta una acción al ser presionado.
 *
 * @param text El texto que se muestra en el botón.
 * @param onClick Acción que se ejecuta al hacer clic en el botón.
 * @param modifier Modificador opcional para personalizar el botón.
 */
@Composable
fun CustomButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        modifier = modifier.padding(8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}




/**
 * Función composable que representa la pantalla de la Liga Americana.
 *
 * @param navController Controlador de navegación para manejar la navegación entre pantallas.
 * @param favoritosViewModel ViewModel que gestiona la lista de equipos favoritos.
 */
@Composable
fun PantallaAmericana(navController: NavController, favoritosViewModel: FavoritosViewModel) {
    // Lista de equipos de la Liga Americana
    val equipos: List<Equipo> = listOf(
        Equipo("Baltimore Orioles", "Fueron fundados en 1894\nHan ganado nueve series\n Mundiales", R.drawable.orioles),
        Equipo("New York Yankees", "Fueron fundados en 1903\nHan ganado 27 series\n Mundiales", R.drawable.yankees),
        Equipo("Boston Red Sox", "Fueron fundados en 1901\nHan ganado 9 Series\n Mundiales", R.drawable.boston),
        Equipo("Tampa Bay Rays", "Fueron fundados en 1998\nAún no han ganado una\n serie Mundial.", R.drawable.rays),
        Equipo("Toronto Blue Jays", "Fueron fundados en 1977\nHan ganado dos veces\n la Serie Mundial", R.drawable.toronto),
        Equipo("Chicago White Sox", "Fueron fundados en 1901\nHan ganado tres series\n Mundiales", R.drawable.whitesox),
        Equipo("Cleveland Guardians", "Fueron fundados en 1894\nHan ganado dos series\n Mundiales", R.drawable.cleveland),
        Equipo("Detroit Tigers", "Fueron fundados en 1894\nHan ganado cuatro Series\n Mundiales", R.drawable.detroit),
        Equipo("Kansas City Royals", "Fueron fundados en 1969\nHan ganado dos series\n Mundiales", R.drawable.kansascity),
        Equipo("Minnesota Twins", "Fueron fundados en 1901\nHan ganado tres series\n Mundiales", R.drawable.twins),
        Equipo("Los Ángeles Angels", "Fueron fundados en 1961\nHan ganado una serie\n Mundial", R.drawable.angels),
        Equipo("Seattle Mariners", "Fueron fundados en 1977\nAún no han ganado una\n serie Mundial", R.drawable.mariners),
        Equipo("Houston Astros", "Fueron fundados en 1962\nHan ganado dos series\n Mundiales", R.drawable.astros),
        Equipo("Oakland Athletics", "Fueron fundados en 1901\nHan ganado nueve series\n Mundiales", R.drawable.athletics),
        Equipo("Texas Rangers", "Fueron fundados en 1961\nAún no han ganado una\n serie Mundial", R.drawable.rangers)
    )

    // Observa los cambios en la lista de equipos favoritos
    val equiposFavoritos by favoritosViewModel.equiposFavoritos.observeAsState(emptyList())

    Scaffold(
        topBar = { CustomTopBar(navController = navController, title = "Liga Nacional") },
        bottomBar = { CustomBottomBar(navController, favoritosViewModel) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF8B4513))
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(equipos) { equipo ->
                    EquipoCard(
                        equipo = equipo,
                        onFavoriteClick = {
                            favoritosViewModel.agregarEquipoFavorito(equipo)
                        },
                        favoritosViewModel = favoritosViewModel,
                        showFavoriteIcon = true,
                        onCardClick = {
                            navController.navigate("equipo_detail/${equipo.nombre}")
                        }

                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}





/**
 * Función composable que representa la pantalla de la Liga Nacional.
 *
 * @param navController Controlador de navegación para manejar la navegación entre pantallas.
 * @param favoritosViewModel ViewModel que gestiona la lista de equipos favoritos.
 */
@Composable
fun PantallaNacional(navController: NavController, favoritosViewModel: FavoritosViewModel) {

    // Lista de equipos de la Liga Nacional
    val equipos: List<Equipo> = listOf(
        Equipo("Atlanta Braves", "Fueron fundados en 1871\nHan ganado cuatro series\n Mundiales", R.drawable.braves),
        Equipo("Miami Marlins", "Fueron fundados en 1993\nHan ganado dos series\n Mundiales", R.drawable.marlins),
        Equipo("New York Mets", "Fueron fundados en 1962\nHan ganado dos series\n Mundiales", R.drawable.mets),
        Equipo("Philadelphia Phillies", "Fueron fundados en 1883\nHan ganado dos series\n Mundiales", R.drawable.phillies),
        Equipo("Washington Nationals", "Fueron fundados en 1969\nHan ganado una serie\n Mundial", R.drawable.nationals),
        Equipo("Chicago Cubs", "Fueron fundados en 1876\nHan ganado tres series\n Mundiales", R.drawable.cubs),
        Equipo("Cincinnati Reds", "Fueron fundados en 1881\nHan ganado cinco series\n Mundiales", R.drawable.reds),
        Equipo("Milwaukee Brewers", "Fueron fundados en 1969\nAún no han ganado una\n serie Mundial", R.drawable.brewers),
        Equipo("Pittsburgh Pirates", "Fueron fundados en 1882\nHan ganado cinco series\n Mundiales", R.drawable.pirates),
        Equipo("St. Louis Cardinals", "Fueron fundados en 1882\nHan ganado once series\n Mundiales", R.drawable.cardinals),
        Equipo("Arizona Diamondbacks", "Fueron fundados en 1998\nHan ganado una serie\n Mundial", R.drawable.arizona),
        Equipo("Colorado Rockies", "Fueron fundados en 1993\nAún no han ganado una\n serie Mundial", R.drawable.rockies),
        Equipo("Los Angeles Dodgers", "Fueron fundados en 1883\nHan ganado siete series\n Mundiales", R.drawable.dodgers),
        Equipo("San Diego Padres", "Fueron fundados en 1969\nAún no han ganado una\n serie Mundial", R.drawable.padres),
        Equipo("San Francisco Giants", "Fueron fundados en 1883\nHan ganado ocho series\n Mundiales", R.drawable.giants)
    )

    // Observa los cambios en la lista de equipos favoritos
    val equiposFavoritos by favoritosViewModel.equiposFavoritos.observeAsState(emptyList())

    Scaffold(
        topBar = { CustomTopBar(navController = navController, title = "Liga Nacional") },
        bottomBar = { CustomBottomBar(navController, favoritosViewModel) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF8B4513))
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(equipos) { equipo ->
                    EquipoCard(
                        equipo = equipo,
                        onFavoriteClick = {
                            favoritosViewModel.agregarEquipoFavorito(equipo)
                        },
                        favoritosViewModel = favoritosViewModel,
                        showFavoriteIcon = true,
                        onCardClick = {
                            navController.navigate("equipo_detail/${equipo.nombre}")
                        }

                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}





/**
 * Función composable que muestra una imagen.
 *
 * @param modifier El modificador que se aplicará a la imagen.
 */
@Composable
fun DisplayImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.welcome),
        contentDescription = null, // Descripción de contenido para accesibilidad
        contentScale = ContentScale.Crop, // Escala la imagen para llenar el área asignada
        modifier = modifier // Aplicación del modificador
    )
}




/**
 * Función composable que muestra un texto con estilos específicos.
 *
 * @param text El texto que se va a mostrar.
 * @param fontSize El tamaño de la fuente del texto.
 * @param fontWeight El peso de la fuente del texto.
 * @param modifier El modificador que se aplicará al texto (opcional).
 */
@Composable
fun Greeting(text: String, fontSize: androidx.compose.ui.unit.TextUnit, fontWeight: FontWeight, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.White, // Color del texto
        fontSize = fontSize, // Tamaño de la fuente
        fontWeight = fontWeight, // Peso de la fuente
        textAlign = TextAlign.Center, // Alineación del texto
        modifier = modifier // Aplicación del modificador
    )
}




/**
 * Botón composable personalizado que muestra el texto "Explorar" y ejecuta una acción cuando se hace clic en él.
 *
 * @param onClick La acción que se ejecutará cuando se haga clic en el botón.
 * @param modifier El modificador que se aplicará al botón (opcional).
 */
@Composable
fun ExploreButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick, // Acción a ejecutar cuando se hace clic en el botón
        colors = ButtonDefaults.buttonColors(containerColor = Color.White), // Color del botón
        modifier = modifier
            .fillMaxWidth(0.8f) // Ajusta el ancho del botón al 80% del ancho disponible
            .height(50.dp), // Altura del botón
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp) // Forma del botón con esquinas redondeadas
    ) {
        Text(
            text = "Explorar", // Texto que se muestra en el botón
            color = Color.Black, // Color del texto
            fontSize = 16.sp, // Tamaño de la fuente del texto
            fontWeight = FontWeight.Bold // Peso de la fuente del texto
        )
    }
}




/**
 * Pantalla de vista previa que muestra una imagen de bienvenida y un botón para explorar.
 *
 * @param navController Controlador de navegación para la navegación entre pantallas.
 */
@Composable
fun DisplayImagePreview(navController: NavController) {
    MLBTheShowTheme { // Aplica el tema MLBTheShow al contenido de la pantalla
        Box(modifier = Modifier.fillMaxSize()) { // Contenedor principal que ocupa todo el tamaño disponible
            DisplayImage(modifier = Modifier.fillMaxSize()) // Muestra la imagen de bienvenida en el fondo
            // Capa semitransparente sobre la imagen de fondo para mejorar la legibilidad del texto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.8f } // Configura la opacidad al 80%
                    .background(Color.Black) // Color de fondo negro
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), // Agrega un espacio de relleno alrededor del contenido
                horizontalAlignment = Alignment.CenterHorizontally, // Alinea el contenido horizontalmente al centro
                verticalArrangement = Arrangement.Center // Alinea el contenido verticalmente al centro
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Muestra el saludo "Bienvenido a"
                    Greeting(
                        text = "Bienvenido a",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp) // Añade un espacio inferior
                    )
                    // Muestra el título "MLB THE SHOW"
                    Greeting(
                        text = "MLB THE SHOW",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            // Botón para explorar que navega a la pantalla de exploración al hacer clic
            ExploreButton(
                onClick = {
                    navController.navigate("explore") // Navega a la pantalla de exploración al hacer clic
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Alinea el botón en la parte inferior del contenedor
                    .padding(bottom = 64.dp) // Ajusta la distancia entre el botón y el borde inferior
            )

        }
    }
}




/**
 * Pantalla que muestra la lista de equipos favoritos.
 *
 * @param navController Controlador de navegación para la navegación entre pantallas.
 * @param favoritosViewModel ViewModel que contiene la lista de equipos favoritos.
 */
@Composable
fun PantallaFavoritos(
    navController: NavController,
    favoritosViewModel: FavoritosViewModel
) {
    // Observa los cambios en la lista de equipos favoritos
    val equiposFavoritos by favoritosViewModel.equiposFavoritos.observeAsState(initial = emptyList())

    // Pantalla de favoritos
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8B4513)) // Establece el fondo de la pantalla
    ) {
        // Cabecera
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .background(Color.Black)
                .padding(start = 16.dp, end = 16.dp) // Añade relleno a los lados de la cabecera
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón de retroceso
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                // Título de la pantalla
                Text(
                    text = "Equipos Favoritos",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f) // Hace que el título ocupe todo el espacio disponible
                )
                // Puedes añadir un botón de búsqueda u otras acciones aquí si lo deseas
            }
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Hace que la columna ocupe todo el espacio disponible
        ) {
            // Lista de equipos favoritos
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(equiposFavoritos) { equipo ->
                    EquipoCard(
                        equipo = equipo,
                        onFavoriteClick = {}, // Función vacía como placeholder
                        onDeleteClick = {
                            favoritosViewModel.eliminarEquipoFavorito(equipo)
                        },
                        favoritosViewModel = favoritosViewModel,
                        showFavoriteIcon = false, // No mostrar el icono de favoritos en la pantalla de favoritos
                        onCardClick = { // Manejar el clic en la tarjeta para navegar a la pantalla de detalles
                            navController.navigate("equipo_detail/${equipo.nombre}")
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp)) // Espacio entre las tarjetas
                }
            }
        }

        // Barra de navegación en la parte inferior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController.navigate("explore") }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { /* Acción para SEARCH */ }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }
        }
    }
}










/**
 * Composable que muestra una tarjeta para representar un equipo.
 *
 * @param equipo El equipo a mostrar en la tarjeta.
 * @param onFavoriteClick La acción a realizar cuando se hace clic en el icono de favoritos.
 * @param favoritosViewModel El ViewModel que contiene la lógica para manejar equipos favoritos.
 */
@Composable
fun EquipoCard(
    equipo: Equipo,
    onFavoriteClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    favoritosViewModel: FavoritosViewModel,
    showFavoriteIcon: Boolean,
    onCardClick: () -> Unit // Agregar este parámetro
) {
    var isFavorite by equipo.esFavorito
    var showMessage by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(showMessage) {
        if (showMessage) {
            delay(3000) // Espera 3 segundos
            showMessage = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onCardClick),  // Usar el parámetro aquí
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF38471F))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = equipo.imagen),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp) // Tamaño de la imagen
                    .clip(CircleShape) // Imagen circular
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = equipo.nombre,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = equipo.informacion,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            if (showFavoriteIcon) {
                IconButton(onClick = {
                    isFavorite = !isFavorite
                    onFavoriteClick()
                    showMessage = true
                    messageText = if (isFavorite) "Se agregó a favoritos" else "Se eliminó de favoritos"
                }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
            }

            onDeleteClick?.let {
                IconButton(onClick = {
                    onDeleteClick()
                    showMessage = true
                    messageText = "Se eliminó de favoritos"
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                }
            }
        }
    }

    if (showMessage) {
        Snackbar(
            action = {
                TextButton(onClick = { showMessage = false }) {
                    Text(text = "OK")
                }
            }
        ) {
            Text(text = messageText)
        }
    }
}






@Composable
fun SearchDialog(
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Buscar equipo") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSearch(searchText)
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Buscar")
                }
            }
        }
    }
}







@Composable
fun CustomTopBar(navController: NavController, title: String) {
    // Cabecera
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .background(Color.Black)
            .padding(start = 16.dp, end = 16.dp) // Añade relleno a los lados de la cabecera
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón de retroceso
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            // Título de la pantalla dinámico
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f) // Hace que el título ocupe todo el espacio disponible
            )
        }
    }
}






@Composable
fun CustomBottomBar(
    navController: NavController,
    favoritosViewModel: FavoritosViewModel,
) {
    // Estado para controlar la visibilidad del campo de búsqueda
    val showSearchField = remember { mutableStateOf(false) }

    // Estado para almacenar el texto de búsqueda
    var searchText by remember { mutableStateOf("") }

    // Función para realizar la búsqueda
    fun performSearch(query: String) {
        favoritosViewModel.searchTeams(query)
    }

    // Bottom bar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.navigate("explore") }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.White
                )
            }
            IconButton(onClick = { navController.navigate("favoritos") }) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorites",
                    tint = Color.White
                )
            }
            if (showSearchField.value) {
                // Campo de búsqueda integrado en la barra
                TextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        performSearch(it) // Realizar búsqueda en tiempo real
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.5f) // Ajustar el tamaño del campo de búsqueda
                        .background(Color.White, shape = RoundedCornerShape(8.dp)),
                    label = { Text("Buscar equipo") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                searchText = ""
                                showSearchField.value = false
                                performSearch("") // Limpiar la búsqueda
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                )
            } else {
                IconButton(
                    onClick = { showSearchField.value = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }
        }
    }
}



@Composable
fun EquipoDetailScreen(navController: NavController, equipo: Equipo) {

    // Define una variable para el contexto
    val context = LocalContext.current

    // Función para compartir
    fun shareContent() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Detalles del equipo: ${equipo.nombre}")
            putExtra(Intent.EXTRA_TEXT, equipo.detalles)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartir detalles del equipo"))
    }

    // Caja principal que cubre toda la pantalla y establece un fondo marrón
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8B4513))
    ) {
        // Cabecera que contiene la imagen del equipo y la flecha de retroceso
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Imagen del equipo centrada en la cabecera
            Image(
                painter = painterResource(id = equipo.imagen),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.Center)
            )
            // Botón de flecha de retroceso centrado horizontalmente
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)

            ) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.Center),
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            // Botón de compartir en la cabecera
            IconButton(
                onClick = { shareContent() },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
        }

        // Columna que contiene el nombre y los detalles del equipo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(130.dp)) // Espacio ajustado entre la cabecera y el nombre del equipo
            // Nombre del equipo centrado y en letras grandes
            Text(
                text = equipo.nombre,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Detalles del equipo justificados y ocupando todo el ancho disponible
            Text(
                text = equipo.detalles,
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Barra de navegación en la parte inferior de la pantalla
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.Black)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            // Icono de home en la barra de navegación
            IconButton(onClick = { navController.navigate("explore") }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.White
                )
            }
        }
    }
}



