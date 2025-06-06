-- Crear base de datos
CREATE DATABASE ClinicaSaludTotal;
GO

USE ClinicaSaludTotal;
GO

-- Tabla de Usuarios (en español)
CREATE TABLE Usuarios (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Nombre VARCHAR(20) NOT NULL,
    PasswordHash VARCHAR(10) NOT NULL,
    CorreoElectronico VARCHAR(20) NOT NULL UNIQUE,
    Estado TINYINT NOT NULL
);

-- Tabla de Pacientes (con Teléfono en vez de Correo)
CREATE TABLE Pacientes (
    Id INT PRIMARY KEY IDENTITY(1,1),
    NombreCompleto VARCHAR(50) NOT NULL,
    Telefono VARCHAR(9),
    FechaNacimiento DATE NOT NULL
);

-- Tabla de Especialidades
CREATE TABLE Especialidades (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Nombre VARCHAR(30) NOT NULL UNIQUE,
    Descripcion VARCHAR(255)
);

-- Tabla de Médicos (relacionada con Especialidades)
CREATE TABLE Medicos (
    Id INT PRIMARY KEY IDENTITY(1,1),
    NombreCompleto VARCHAR(50) NOT NULL,
    EspecialidadId INT NOT NULL,
    Sueldo DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (EspecialidadId) REFERENCES Especialidades(Id)
);

-- Tabla de Horarios de atención por médico
CREATE TABLE Horarios (
    Id INT PRIMARY KEY IDENTITY(1,1),
    MedicoId INT NOT NULL,
    DiaSemana VARCHAR(15) NOT NULL,
    HoraInicio TIME NOT NULL,
    HoraFin TIME NOT NULL,
    FOREIGN KEY (MedicoId) REFERENCES Medicos(Id)
);

-- Tabla de Citas (relacionada con Pacientes y Médicos)
CREATE TABLE Citas (
    Id INT PRIMARY KEY IDENTITY(1,1),
    PacienteId INT NOT NULL,
    MedicoId INT NOT NULL,
    FechaCita DATE NOT NULL,
    CostoConsulta DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (PacienteId) REFERENCES Pacientes(Id),
    FOREIGN KEY (MedicoId) REFERENCES Medicos(Id)
);