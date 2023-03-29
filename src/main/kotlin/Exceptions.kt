package gitinternals

class DirectoryNotSetException : Exception("El directorio no ha sido inicializado.")

class NotSuchBranchException(branch: String) : Exception("No existe una rama con el nombre indicado -$branch-.")