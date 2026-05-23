import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

interface HerramientaConversion {
  id: string;
  titulo: string;
  descripcion: string;
  tipo: 'audio' | 'video' | 'imagen';
  formatos: string[];
  color: string;
  icono: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {

  herramientas: HerramientaConversion[] = [
    {
      id: 'audio',
      titulo: 'Convertir Audio',
      descripcion: 'Convierte tus archivos de audio entre los formatos más populares con alta calidad.',
      tipo: 'audio',
      formatos: ['MP3', 'WAV', 'AAC', 'FLAC', 'OGG', 'M4A'],
      color: '#FF6B4A',
      icono: 'audio'
    },
    {
      id: 'video',
      titulo: 'Convertir Video',
      descripcion: 'Transforma tus videos a cualquier formato compatible con todos tus dispositivos.',
      tipo: 'video',
      formatos: ['MP4', 'MKV', 'AVI', 'MOV', 'WEBM', 'FLV'],
      color: '#4361EE',
      icono: 'video'
    },
    {
      id: 'imagen',
      titulo: 'Convertir Imagen',
      descripcion: 'Cambia el formato de tus imágenes manteniendo la mejor calidad posible.',
      tipo: 'imagen',
      formatos: ['JPG', 'PNG', 'WEBP', 'GIF', 'BMP', 'TIFF'],
      color: '#2EC4B6',
      icono: 'imagen'
    }
  ];

  constructor(private router: Router, public auth: AuthService) {}

  irAConvertir(tipo: string): void {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.router.navigate(['/convertir', tipo]);
  }
}
