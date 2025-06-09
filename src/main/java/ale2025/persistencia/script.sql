USE ClinicaSaludTotal;
GO

CREATE TABLE Users (
    id INT PRIMARY KEY IDENTITY(1,1),
    name VARCHAR(100) NOT NULL,
    passwordHash VARCHAR(64) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    status TINYINT NOT NULL
);
GO

CREATE TABLE Pacientes (
    id INT PRIMARY KEY IDENTITY(1,1),
    nombreCompleto VARCHAR(50) NOT NULL,
    telefono VARCHAR(9),
    fechaNacimiento DATE NOT NULL
);
GO

CREATE TABLE Especialidades (
    id INT PRIMARY KEY IDENTITY(1,1),
    nombre VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);
GO

CREATE TABLE Medicos (
    id INT PRIMARY KEY IDENTITY(1,1),
    nombreCompleto VARCHAR(50) NOT NULL,
    especialidadId INT NOT NULL,
    sueldo DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (especialidadId) REFERENCES Especialidades(id)
);
GO

CREATE TABLE Horarios (
    id INT PRIMARY KEY IDENTITY(1,1),
    medicoId INT NOT NULL,
    diaSemana VARCHAR(15) NOT NULL,
    horaInicio TIME NOT NULL,
    horaFin TIME NOT NULL,
    FOREIGN KEY (medicoId) REFERENCES Medicos(id)
);
GO

CREATE TABLE Citas (
    id INT PRIMARY KEY IDENTITY(1,1),
    pacienteId INT NOT NULL,
    medicoId INT NOT NULL,
    fechaCita DATE NOT NULL,
    costoConsulta DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pacienteId) REFERENCES Pacientes(id),
    FOREIGN KEY (medicoId) REFERENCES Medicos(id)
);
GO