package com.example.mlbtheshow

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
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
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
    }
}




/**
 * ViewModel para gestionar la lista de equipos favoritos.
 */
class FavoritosViewModel : ViewModel() {

    // Lista mutable de equipos favoritos
    private val _equiposFavoritos = MutableLiveData<List<Equipo>>()
    // Exponemos una LiveData inmutable para que otras clases solo puedan observarla, no modificarla directamente
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


    fun toggleEquipoFavorito(equipo: Equipo) {
        if (_equiposFavoritos.value?.contains(equipo) == true) {
            eliminarEquipoFavorito(equipo)
        } else {
            agregarEquipoFavorito(equipo)
        }
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
        topBar = { CustomTopBar(navController = navController, title = "Liga Americana")},
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
                            if (favoritosViewModel.equiposFavoritos.value?.contains(equipo) == true) {
                                favoritosViewModel.eliminarEquipoFavorito(equipo)
                            } else {
                                favoritosViewModel.agregarEquipoFavorito(equipo)
                            }
                        },
                        favoritosViewModel = favoritosViewModel,
                        showFavoriteIcon = true
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
                            if (favoritosViewModel.equiposFavoritos.value?.contains(equipo) == true) {
                                favoritosViewModel.eliminarEquipoFavorito(equipo)
                            } else {
                                favoritosViewModel.agregarEquipoFavorito(equipo)
                            }
                        },
                        favoritosViewModel = favoritosViewModel,
                        showFavoriteIcon = true
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
                        showFavoriteIcon = false // No mostrar el icono de favoritos en la pantalla de favoritos
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
    showFavoriteIcon: Boolean
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
            .clickable(onClick = {}),
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
    // Estado para controlar la visibilidad del diálogo de búsqueda
    val showDialog = remember { mutableStateOf(false) }

    // Estado para almacenar el texto de búsqueda
    var searchText by remember { mutableStateOf("") }

    // Lista mutable de equipos filtrados por búsqueda
    val equiposFiltrados = remember { mutableStateListOf<Equipo>() }

    // Función para realizar la búsqueda y actualizar la lista de equipos filtrados
    fun performSearch(query: String) {
        equiposFiltrados.clear()
        val listaEquipos = favoritosViewModel.equiposFavoritos.value ?: emptyList()
        equiposFiltrados.addAll(listaEquipos.filter { it.nombre.contains(query, ignoreCase = true) })
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
            IconButton(onClick = { navController.navigate("explore")}) {
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
            // IconButton para abrir el diálogo de búsqueda
            IconButton(
                onClick = { showDialog.value = true },
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        }

    }

    // Diálogo de búsqueda
    if (showDialog.value) {
        SearchDialog(
            onSearch = { searchText ->
                performSearch(searchText)
                // Aquí podrías ejecutar alguna acción adicional si es necesario
                showDialog.value = false // Cerrar el diálogo después de la búsqueda
            },
            onDismiss = { showDialog.value = false }
        )
    }
}





