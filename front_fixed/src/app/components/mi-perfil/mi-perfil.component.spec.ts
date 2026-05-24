import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MiPerfilComponent } from './mi-perfil.component';

describe('MiPerfilComponent', () => {
    let component: MiPerfilComponent;
    let fixture: ComponentFixture<MiPerfilComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [MiPerfilComponent],
            imports: [HttpClientTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(MiPerfilComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});