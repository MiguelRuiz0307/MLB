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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.mlbtheshow.ui.theme.MLBTheShowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MLBTheShowTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "welcome") {
                    composable("welcome") {
                        WelcomeScreen(navController = navController)
                    }
                    composable("explore") {
                        ExploreScreen(navController = navController)
                    }
                    composable("pantalla_americana") {
                        PantallaAmericana(navController = navController)
                    }
                    composable("pantalla_nacional") {
                        PantallaNacional(navController = navController)
                    }
                }
            }
        }
    }
}



@Composable
fun WelcomeScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        DisplayImage(modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.8f }
                .background(Color.Black)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Greeting(
                    text = "Bienvenido a",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Greeting(
                    text = "MLB THE SHOW",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        ExploreButton(
            onClick = { navController.navigate("explore") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp) // Ajusta la distancia entre el botón y el borde inferior
        )
    }
}



@Composable
fun ExploreScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8B4513))
    ) {
        // Cabecera con la imagen, flecha y título
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Image(
                painter = painterResource(id = R.drawable.menu),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                // Flecha hacia atrás
                IconButton(
                    onClick = { navController.navigate("welcome") }, // Navegar de nuevo a la pantalla de welcome
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

        // Agregar los botones y las imágenes debajo de cada botón
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 150.dp)  // Ajusta la distancia desde la parte superior de la pantalla según sea necesario
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CustomButton(
                        text = "Liga Americana",
                        onClick = { navController.navigate("pantalla_americana") }
                    )
                    Image(
                        painter = painterResource(id = R.drawable.americana),
                        contentDescription = null,
                        modifier = Modifier
                            .width(100.dp)  // Ajusta el ancho de la imagen según sea necesario
                            .height(100.dp)  // Ajusta la altura de la imagen según sea necesario
                            .padding(top = 8.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CustomButton(
                        text = "Liga Nacional",
                        onClick = { navController.navigate("pantalla_nacional") }
                    )
                    Image(
                        painter = painterResource(id = R.drawable.nacional),
                        contentDescription = null,
                        modifier = Modifier
                            .width(100.dp)  // Ajusta el ancho de la imagen según sea necesario
                            .height(100.dp)  // Ajusta la altura de la imagen según sea necesario
                            .padding(top = 8.dp)
                    )
                }
            }

            // Agregar la nueva imagen centrada debajo de los botones y las imágenes
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.iconopantalla),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp, top = 40.dp) // Añadir padding a los lados y arriba
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}



@Composable
fun CustomButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        modifier = modifier
            .padding(8.dp),
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


@Composable
fun PantallaAmericana(navController: NavController) {

    // Supongamos que tienes una lista de equipos
    val equipos: List<Equipo> = listOf(
        Equipo("Baltimore Orioles", "Ubicación: Baltimore, Maryland, Estados Unidos\n" +
                "Estadio: Oriole Park at Camden Yards\n" +
                "Mascota: The Oriole Bird\n" +
                "Campeonatos de la Serie Mundial: 1983, 1970, 1966", R.drawable.orioles),

        Equipo("New York Yankees", "Campeonatos de la Serie Mundial: 2009, 2000, 1999, 1998, 1996, MÁS\n" +
                "Ubicación: El Bronx, Nueva York, Estados Unidos\n" +
                "Estadio: Yankee Stadium", R.drawable.yankees),

        Equipo("Boston Red Sox", "Ubicaciones: Boston, Massachusetts, Estados Unidos\n" +
                "Estadio: Fenway Park\n" +
                "Campeonatos de la Serie Mundial: 2018, 2013, 2007, 2004, 1918, 1916, 1915, 1912, 1903", R.drawable.boston),

        Equipo("Tampa Bay Rays ", "Ubicación: Área de la Bahía de Tampa, Florida, Estados Unidos\n" +
                "Estadios: Tropicana Field, Charlotte Sports Park\n" +
                "Fundación: 1998 (26 años)", R.drawable.rays),

        Equipo("Toronto Blue Jays", "Ubicación: Toronto, Canadá\n" +
                "Mascota: Ace\n" +
                "Campo: Rogers Centre\n" +
                "Fundación: 1977 (47 años)", R.drawable.toronto),

        Equipo("Chicago White Sox", "Estadio: Guaranteed Rate Field\n" +
                "Ubicación: Chicago, Illinois, Estados Unidos", R.drawable.whitesox),

        Equipo("Cleveland Guardians", "Información del equipo", R.drawable.cleveland),

        Equipo("Detroit Tigers", "Información del equipo", R.drawable.detroit),

        Equipo("Kansas City Royals", "Información del equipo", R.drawable.kansascity),

        Equipo("Minnesota Twins", "Información del equipo", R.drawable.twins),

        Equipo("Los Ángeles Angels", "Información del equipo", R.drawable.angels),

        Equipo("Seattle Mariners", "Información del equipo", R.drawable.mariners),

        Equipo("Houston Astros ", "Información del equipo", R.drawable.astros),

        Equipo("Oakland Athletics ", "Información del equipo", R.drawable.athletics),

        Equipo("Texas Rangers", "Información del equipo", R.drawable.rangers),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8B4513)) // Mismo fondo que la pantalla de explorar sin padding
    ) {
        // Cabecera
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp) // Altura de la cabecera aumentada
                .background(Color.Black)
                .padding(start = 16.dp, end = 16.dp) // Padding horizontal para la cabecera
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically, // Centrar verticalmente los elementos de la cabecera
                horizontalArrangement = Arrangement.SpaceBetween // Espacio entre elementos
            ) {
                // Flecha hacia atrás para regresar a la pantalla anterior
                IconButton(
                    onClick = { navController.popBackStack() }, // Función para regresar a la pantalla anterior
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                // Título "Liga Americana"
                Text(
                    text = "Liga Americana",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, // Centrar horizontalmente el título
                    modifier = Modifier.weight(1f) // Ocupa el espacio restante en la cabecera
                )
                // Imagen "americana"
                Image(
                    painter = painterResource(id = R.drawable.americana),
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp) // Tamaño de la imagen
                        .align(Alignment.CenterVertically) // Centrar verticalmente la imagen
                )
            }
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Lista de equipos
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(equipos) { equipo ->
                    EquipoCard(equipo = equipo, onClick = { /* Acción al hacer clic en la tarjeta */ })
                    Spacer(modifier = Modifier.height(8.dp)) // Agregar espacio entre tarjetas
                }
            }

        }

        // Footer
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



@Composable
fun PantallaNacional(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8B4513)) // Mismo fondo que la pantalla de explorar sin padding
    ) {
        // Cabecera
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp) // Altura de la cabecera aumentada
                .background(Color.Black)
                .padding(start = 16.dp, end = 16.dp) // Padding horizontal para la cabecera
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically, // Centrar verticalmente los elementos de la cabecera
                horizontalArrangement = Arrangement.SpaceBetween // Espacio entre elementos
            ) {
                // Flecha hacia atrás para regresar a la pantalla anterior
                IconButton(
                    onClick = { navController.popBackStack() }, // Función para regresar a la pantalla anterior
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                // Título "Liga Americana"
                Text(
                    text = "Liga Americana",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, // Centrar horizontalmente el título
                    modifier = Modifier.weight(1f) // Ocupa el espacio restante en la cabecera
                )
                // Imagen "americana"
                Image(
                    painter = painterResource(id = R.drawable.nacional),
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp) // Tamaño de la imagen
                        .align(Alignment.CenterVertically) // Centrar verticalmente la imagen
                )
            }
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Aquí puedes agregar el contenido principal de la pantalla de "Liga Americana"
            // Por ejemplo, información sobre la liga americana, lista de equipos, etc.
        }

        // Footer
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





@Composable
fun DisplayImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.welcome),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@Composable
fun Greeting(text: String, fontSize: androidx.compose.ui.unit.TextUnit, fontWeight: FontWeight, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.White,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
fun ExploreButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        modifier = modifier
            .fillMaxWidth(0.8f) // Ajusta el ancho del botón
            .height(50.dp), // Ajusta la altura del botón
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "Explorar",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DisplayImagePreview(navController: NavController) {
    MLBTheShowTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            DisplayImage(modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.8f }
                    .background(Color.Black)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Greeting(
                        text = "Bienvenido a",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Greeting(
                        text = "MLB THE SHOW",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            ExploreButton(
                onClick = {
                    navController.navigate("explore")
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp) // Ajusta la distancia entre el botón y el borde inferior
            )

        }
    }
}


@Composable
fun EquipoCard(equipo: Equipo, onClick: () -> Unit) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF38471),
        contentColor = Color.White
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        ){
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen del equipo
                Image(
                    painter = painterResource(id = equipo.imagen),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )

                // Nombre del equipo
                Text(
                    text = equipo.nombre,
                    modifier = Modifier.padding(start = 16.dp)
                )

                // Espacio entre el nombre del equipo y los iconos
                Spacer(modifier = Modifier.weight(1f))

                // Icono de favoritos
                IconButton(
                    onClick = { /* Acción al hacer clic en el icono de favoritos */ },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favoritos",
                        tint = Color.Gray // Color de los iconos
                    )
                }

                // Icono de eliminar
                IconButton(
                    onClick = { /* Acción al hacer clic en el icono de eliminar */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Gray // Color de los iconos
                    )
                }
            }
        }
    }
}




data class Equipo(
    val nombre: String,
    val informacion: String,
    val imagen: Int
)
